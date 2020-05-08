 package dta_solver;
 
 import generalLWRNetwork.Cell;
 import generalLWRNetwork.Junction;
 import generalNetwork.state.CellInfo;
 import generalNetwork.state.JunctionInfo;
 import generalNetwork.state.State;
 import generalNetwork.state.internalSplitRatios.IntertemporalSplitRatios;
 import generalNetwork.state.internalSplitRatios.JunctionSplitRatios;
 import cern.colt.matrix.tdouble.DoubleMatrix1D;
 import cern.colt.matrix.tdouble.DoubleFactory1D;
 import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
 import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
 import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
 import dataStructures.Numerical;
 
 public class SOPC_Optimizer extends SO_Optimizer {
 
   public SOPC_Optimizer(int maxIter, Simulator simu) {
     super(maxIter, simu);
 
     simulator.initializSplitRatios();
   }
 
   /**
    * @brief Computes the derivative dJ/dU
    * @details
    *          The condition \beta >= 0 is already put in the solver (in
    *          AdjointJVM/org.wsj/Optimizers.scala) do there is only one barrier
    *          in J
    */
   @Override
   public DoubleMatrix1D djdu(State state, double[] control) {
     return DoubleFactory1D.dense.make(T * temporal_control_block_size);
   }
 
   /**
    * @details This function imposes that the control is physical (every split
    *          ratio is positive)
    */
   @Override
   public double objective(double[] control) {
     /* Inforces control[i] >= 0, \forall i */
     for (int i = 0; i < control.length; i++)
       if (control[i] < 0)
         assert false : "Negative control " + control[i];
     // return Double.MAX_VALUE;
 
     return objective(forwardSimulate(control), control);
   }
 
   /**
    * @brief Computes the objective function:
    *        \sum_(i,c,k) \rho(i,c,k)
    *        - \sum_{origin o} epsilon2 * ln(\sum \rho(o,c,k) - 1)
    * @details
    *          The condition \beta >= 0 is already put in the solver (in
    *          AdjointJVM/org.wsj/Optimizers.scala) do there is only one barrier
    *          in J
    */
   public double objective(State state, double[] control) {
     double objective = 0;
 
     /*
      * To compute the sum of the densities ON the network, we add the density of
      * all the cells and then remove the density of the sinks
      */
     for (int k = 0; k < T; k++) {
       for (int cell_id = 0; cell_id < cells.length; cell_id++)
         objective += state.profiles[k].getCell(cell_id).total_density;
 
       for (int d = 0; d < destinations.length; d++)
         objective -= state.profiles[k].getCell(destinations[d].getUniqueId()).total_density;
     }
 
     return objective;
   }
 
   /* Returns the position of rho(i, c)(k) */
   private int rho(int k, int i, int c) {
     return k * x_block_size + (C + 1) * i + c;
   }
 
   private int f_in(int k, int i, int c) {
     return k * x_block_size + f_in_position + (C + 1) * i + c;
   }
 
   private int f_out(int k, int i, int c) {
     return k * x_block_size + f_out_position + (C + 1) * i + c;
   }
 
   public DoubleMatrix1D lambdaByAdjointMethod(State state, double[] control) {
 
     DoubleMatrix1D lambda = new DenseDoubleMatrix1D(T * x_block_size);
     double delta_t = simulator.time_discretization.getDelta_t();
     IntertemporalSplitRatios internal_SR =
         simulator.lwr_network.getInternal_split_ratios();
 
     for (int k = T - 1; k >= 0; k--) {
 
       if (k < T - 1) {
         /* We first solve f_in */
         for (int cell_id = 0; cell_id < cells.length; cell_id++) {
           for (int c = 0; c < (C + 1); c++) {
             if (!cells[cell_id].isBuffer() && !cells[cell_id].isSink()) {
               double value = delta_t / cells[cell_id].getLength()
                   * lambda.get(rho(k + 1, cell_id, c));
               assert Numerical.validNumber(value);
               lambda.set(f_in(k, cell_id, c), value);
             }
           }
         }
       }
 
       /* We solve f_out */
       for (int junction_id = 0; junction_id < junctions.length; junction_id++) {
         Junction junction = junctions[junction_id];
 
         Cell[] in_links = junction.getPrev();
         Cell[] out_links = junction.getNext();
         JunctionSplitRatios junction_SR = internal_SR.get(k, junction_id);
 
         for (int c = 0; c < (C + 1); c++) {
           for (int in_link = 0; in_link < in_links.length; in_link++) {
             int in_link_id = in_links[in_link].getUniqueId();
 
             double value = 0;
             for (int out_link = 0; out_link < out_links.length; out_link++) {
               int out_link_id = out_links[out_link].getUniqueId();
 
               double beta;
               /* For the Nx1 junctions the split ratios are always 1 */
               if (junction.isMergingJunction()) {
                 assert junction_SR == null;
                 beta = 1;
                 /* For other junctions, it is registered except if it is 0 */
               } else {
                 assert junction_SR != null;
                 Double beta_res = junction_SR.get(in_link_id, out_link_id, c);
 
                 if (beta_res == null)
                   continue;
                 beta = beta_res.doubleValue();
               }
               value += beta * lambda.get(f_in(k, out_link_id, c));
             }
 
             if (k < T - 1)
               value -= delta_t / cells[in_link_id].getLength() *
                   lambda.get(rho(k + 1, in_link_id, c));
 
             assert Numerical.validNumber(value);
             lambda.set(f_out(k, in_link_id, c), value);
           }
         }
       }
 
       /* We solve the aggregate split ratios */
       /*
        * int aggregate_index = 0;
        * for (int j_id = 0; j_id < junctions.length; j_id++) {
        * Junction junction = junctions[j_id];
        * JunctionInfo junction_info = state.get(k).getJunction(j_id);
        * Cell[] in_links = junction.getPrev();
        * Cell[] out_links = junction.getNext();
        * int nb_prev = in_links.length;
        * int nb_next = out_links.length;
        * 
        * // The split ratios are not used for Nx1 junctions
        * if (junction.isMergingJunction())
        * continue;
        * 
        * if (nb_prev != 1) {
        * System.out.println("The (n>1)xm junctions are not implemented");
        * System.exit(1);
        * }
        * assert nb_prev == 1;
        * assert nb_next > 1;
        * for (int in_link = 0; in_link < nb_prev; in_link++) {
        * for (int out_link = 0; out_link < nb_next; out_link++) {
        * int in_link_id = in_links[in_link].getUniqueId();
        * CellInfo in_cell_info = state.get(k).getCell(in_link_id);
        * double total_density = in_cell_info.total_density;
        * if (total_density == 0)
        * continue;
        * 
        * if (junction_info.is_supply_limited()) {
        * assert total_density != 0;
        * int limiting_supply = state.get(k).junction_info[j_id]
        * .getLimiting_supply();
        * assert limiting_supply != -1;
        * Double beta = state.get(k).junction_info[j_id].getAggregateSR(
        * in_link_id, limiting_supply);
        * assert beta != null;
        * double out_flow = state.get(k).junction_info[j_id]
        * .getFlowOut(in_link_id);
        * 
        * double value = 0;
        * for (int c = 0; c < C + 1; c++) {
        * Double partial_density = in_cell_info.partial_densities.get(c);
        * if (partial_density == null)
        * continue;
        * value -= partial_density / total_density / beta * out_flow;
        * }
        * lambda.set(k * x_block_size + aggregate_split_ratios_position
        * + aggregate_index + in_link * nb_next
        * + out_link, value);
        * }
        * // If it is not supply limited, the split ratios has no effect
        * else {
        * // lambda.set(k * x_block_size + aggregate_split_ratios_position
        * // + aggregate_index + in_link * nb_next
        * // + out_link, 0); // Double.NaN
        * }
        * }
        * }
        * aggregate_index += nb_prev * nb_next;
        * }
        */
 
       /* We solve the partial densities */
       for (int cell_id = 0; cell_id < cells.length; cell_id++) {
         /* The increase of the density of a sink has no influence */
         if (cells[cell_id].isSink())
           continue;
         for (int c = 0; c < (C + 1); c++) {
           double value = cells[cell_id].getLength();
           if (k < T - 1)
             value += lambda.get(rho(k + 1, cell_id, c));
           assert Numerical.validNumber(value);
           lambda.set(rho(k, cell_id, c), value);
         }
       }
 
       if (k == T - 1)
         continue;
 
       for (int j_id = 0; j_id < junctions.length; j_id++) {
         Junction junction = junctions[j_id];
         JunctionInfo junction_info = state.get(k).getJunction(j_id);
         Cell[] in_links = junction.getPrev();
         Cell[] out_links = junction.getNext();
         int nb_prev = in_links.length;
         int nb_next = out_links.length;
 
         // 1xN junctions
         if (nb_prev == 1) {
           if (junction_info.is_demand_limited()) {
 
             int limiting_demand_id = in_links[0].getUniqueId();
             double total_density = state.get(k).getCell(limiting_demand_id).total_density;
 
             for (int c = 0; c < (C + 1); c++) {
               double value = lambda.get(rho(k, limiting_demand_id, c))
                   + in_links[0].getDerivativeDemand(total_density, delta_t)
                   * lambda.get(f_out(k, limiting_demand_id, c));
               assert Numerical.validNumber(value);
               lambda.set(rho(k, limiting_demand_id, c), value);
             }
 
           } else if (junction_info.is_supply_limited()) {
             int limiting_outgoing_link_id = junction_info.getLimiting_supply();
             Cell limiting_outgoing_link = cells[limiting_outgoing_link_id];
             // 1xN junctions
             if (nb_prev == 1) {
 
               int in_cell_id = in_links[0].getUniqueId();
               CellInfo in_cell = state.get(k).getCell(in_cell_id);
               double total_density = in_cell.total_density;
               assert total_density != 0;
               double value = 0;
 
               Double aggr_beta = junction_info.getAggregateSR(in_cell_id,
                   limiting_outgoing_link_id);
               assert (aggr_beta != 0 && aggr_beta != null);
 
               /* We compute the upstream effect */
               for (int c = 0; c < (C + 1); c++) {
                 Double partial_density = in_cell.partial_densities.get(c);
 
                 if (partial_density == null)
                   continue;
                 value += partial_density / total_density / aggr_beta *
                     lambda.get(f_out(k, in_cell_id, c));
               }
               double limiting_density = state
                   .get(k)
                   .getCell(limiting_outgoing_link).total_density;
               double backspeed = limiting_outgoing_link
                   .getDerivativeSupply(limiting_density);
 
               value = backspeed * value;
 
               assert Numerical.validNumber(value);
               for (int c = 0; c < (C + 1); c++) {
                 lambda.set(rho(k, limiting_outgoing_link_id, c),
                     lambda.get(rho(k, limiting_outgoing_link_id, c)) + value);
               }
 
               /* We compute the downstream effect */
               double supply = junction_info.getFlowOut(in_cell_id) * aggr_beta;
               double rho_aggrSR = total_density * aggr_beta;
               /* Update of rho(k, in_cell_id, c) */
               for (int c = 0; c < (C + 1); c++) {
 
                 for (int c2 = 0; c2 < (C + 1); c2++) {
                   Double partial_density = in_cell.partial_densities.get(c2);
                   double tmp_value = 0;
                   if (partial_density == null)
                     partial_density = 0.0;
 
                   JunctionSplitRatios JSR = internal_SR.get(k, j_id);
                   double SR;
                   if (JSR == null) {
                     SR = 1;
                   } else {
                     Double res = JSR.get(in_cell_id, limiting_outgoing_link_id,
                         c);
                     if (res == null)
                       SR = 0;
                     else
                       SR = res.doubleValue();
                   }
                   if (c2 == c) {
                     tmp_value =
                         (total_density * aggr_beta) - partial_density * SR;
                   } else {
                     tmp_value = -partial_density * SR;
                   }
 
                   tmp_value *= supply / (total_density * aggr_beta)
                       / (total_density * aggr_beta);
                   assert Numerical.validNumber(tmp_value);
                   lambda.set(rho(k, in_cell_id, c),
                       lambda.get(rho(k, in_cell_id, c)) + tmp_value
                           * lambda.get(f_out(k, in_cell_id, c2)));
                 }
 
               }
             } else {
               System.out.println("Case not handled yet");
               System.exit(1);
             }
           } else {
             printAlert(j_id, k);
             // System.out.println(junction_info.toString());
           }
           // 2x1 junctions
         } else if (nb_prev == 2 && nb_next == 1) {
 
           int demand_priority = junction_info.getPriority_2x1_demand();
 
           // The junction exactly respects the priority constraint
           if (demand_priority == -1) {
             if (junction_info.is_demand_limited()) {
 
               int[] list = new int[] { in_links[0].getUniqueId(),
                   in_links[1].getUniqueId() };
               // We compute the downstream cost for the links
               for (int i = 0; i < 2; i++) {
                 int id = list[i];
                 CellInfo info = state
                     .get(k)
                     .getCell(id);
                 double total_density = info.total_density;
                 double coefficient = cells[id].getDerivativeDemand(
                     total_density,
                     delta_t);
                 if (coefficient != 0)
                   for (int c = 0; c < (C + 1); c++) {
                     lambda.set(rho(k, id, c),
                         lambda.get(rho(k, id, c)) + coefficient
                             * lambda.get(f_out(k, id, c)));
                   }
               }
 
             } else if (junction_info.is_supply_limited()) {
 
               int[] list = new int[] { in_links[0].getUniqueId(),
                   in_links[1].getUniqueId() };
               // We compute the downstream cost for the incoming links
               for (int i = 0; i < 2; i++) {
                 int id = list[i];
                 CellInfo info = state
                     .get(k)
                     .getCell(id);
                 double total_density = info.total_density;
                 if (total_density == 0) {
                   System.out
                       .println("Strange behavior. Look at SOPC for 2x1 junctions");
                   System.exit(1);
                 }
                 double flow = junction_info.getFlowOut(id);
 
                 double common_value = 0;
                 for (int c = 0; c < (C + 1); c++) {
                   Double partial_density = info.partial_densities.get(c);
                   if (partial_density == null || partial_density == 0)
                     continue;
                   common_value += partial_density / total_density
                       * lambda.get(f_out(k, id, c));
                 }
 
                 assert Numerical.validNumber(common_value);
                 for (int c = 0; c < (C + 1); c++) {
                   lambda.set(rho(k, id, c),
                       lambda.get(rho(k, id, c)) +
                           flow / total_density *
                           (lambda.get(f_out(k, id, c)) - common_value));
                 }
               }
 
               // We compute the upsteam cost for the outgoing link
               int out_id = out_links[0].getUniqueId();
               double coefficient = cells[out_id].getDerivativeSupply(state
                   .get(k)
                   .getCell(out_id).total_density);
               if (coefficient == 0)
                 continue;
 
               double value = 0;
               for (int i = 0; i < 2; i++) {
                 int id = list[i];
                 CellInfo info = state
                     .get(k)
                     .getCell(id);
                 double total_density = info.total_density;
                 Double priority = junctions[j_id].getPriority(id);
                 assert total_density != 0 && priority != 0
                     && priority != null;
 
                 for (int c = 0; c < (C + 1); c++) {
                   Double partial_density = info.partial_densities.get(c);
                   if (partial_density == null || partial_density == 0)
                     continue;
                   value += partial_density * priority / total_density
                       * lambda.get(f_out(k, id, c));
                 }
               }
               value *= coefficient;
 
               for (int c = 0; c < (C + 1); c++) {
                 lambda.set(rho(k, out_id, c),
                     lambda.get(rho(k, out_id, c)) + value);
               }
             } else {
                 printAlert(j_id, k);
             }
 
           } else {
             // The junction does not respect the priority constraint and link
             // demand_priority has its demand fulfilled while the other one has
             // not
             if (junction_info.is_demand_limited()) {
 
               int[] list = new int[] { in_links[0].getUniqueId(),
                   in_links[1].getUniqueId() };
               // We compute the downstream cost for the links
               for (int i = 0; i < 2; i++) {
                 int id = list[i];
                 CellInfo info = state
                     .get(k)
                     .getCell(id);
                 double total_density = info.total_density;
                 double coefficient = cells[id].getDerivativeDemand(
                     total_density,
                     delta_t);
                 if (coefficient != 0)
                   for (int c = 0; c < (C + 1); c++) {
                     lambda.set(rho(k, id, c),
                         lambda.get(rho(k, id, c)) + coefficient
                             * lambda.get(f_out(k, id, c)));
                   }
               }
             } else if (junction_info.is_supply_limited()) {
 
               int not_satisfied_link = -1;
               if (demand_priority == in_links[0].getUniqueId())
                 not_satisfied_link = in_links[1].getUniqueId();
               else if (demand_priority == in_links[1].getUniqueId())
                 not_satisfied_link = in_links[0].getUniqueId();
               else {
                 System.out.println("Illegal not satisfied link");
                 System.exit(1);
               }
 
               CellInfo info = state
                   .get(k)
                   .getCell(not_satisfied_link);
               double total_density = info.total_density;
 
               double value = 0;
               for (int c = 0; c < (C + 1); c++) {
                 Double partial_density = info.partial_densities.get(c);
                 if (partial_density == null || partial_density == 0)
                   continue;
                 value += partial_density / total_density
                     * lambda.get(f_out(k, not_satisfied_link, c));
               }
 
               // We compute the downstream cost for the incoming links
               double coef = cells[demand_priority].getDerivativeDemand(state
                   .get(k)
                   .getCell(demand_priority).total_density,
                   delta_t);
               for (int c = 0; c < (C + 1); c++) {
                 lambda.set(rho(k, demand_priority, c),
                     lambda.get(rho(k, demand_priority, c)) +
                         coef
                         * (lambda.get(f_out(k, demand_priority, c)) - value));
               }
               coef = junction_info.getFlowOut(not_satisfied_link) /
                   state
                       .get(k)
                       .getCell(not_satisfied_link).total_density;
               assert (Numerical.validNumber(coef));
 
               for (int c = 0; c < (C + 1); c++) {
                 lambda.set(rho(k, demand_priority, c), lambda.get(rho(k,
                     not_satisfied_link, c))
                     + coef
                     * (lambda.get(f_out(k, not_satisfied_link, c)) - value));
               }
 
               // We compute the upsteam cost for the outgoing link
               int out_id = out_links[0].getUniqueId();
 
               coef = cells[out_id].getDerivativeSupply(state.get(k).getCell(
                   out_id).total_density);
               for (int c = 0; c < (C + 1); c++) {
                 lambda.set(rho(k, out_id, c),
                     lambda.get(rho(k, out_id, c)) + coef * value);
               }
             } else {
               printAlert(j_id, k);
             }
           }
         } else {
           System.out.println("Case not handled yet");
           System.exit(1);
         }
       }
     }
 
     return lambda;
   }
 
   private void printAlert(int j_id, int k) {
     System.err
         .println("[Critical]The junction "
             + j_id
             + " at time step "
             + k
            + " is neither demand nor supply limited. Adjoint descent not defined ! Supply limited applied.");
   }
 
   public double[] gradientByAdjointMethod(State state, double[] control) {
 
     DoubleMatrix1D lambda = lambdaByAdjointMethod(state, control);
     DenseDoubleAlgebra dAlg = new DenseDoubleAlgebra();
     SparseCCDoubleMatrix2D dhduT = dhdu(state, control).getTranspose();
     DoubleMatrix1D gradient = dAlg.mult(dhduT, lambda);
 
     return gradient.toArray();
   }
 
   /**
    * @brief We project the gradient given by the adjoint
    */
   public void gradient(double[] gradient_f, double[] control) {
     State state = forwardSimulate(control);
     double[] g2 = gradientByAdjointMethod(state, control);
     projectGradient(gradient_f, g2);
   }
 
   public void projectGradient(double[] gradient_f, double[] init_gradient) {
     for (int k = 0; k < T; k++) {
       int index = 0;
       for (int o = 0; o < O; o++) {
         double average = 0;
         int nb_commodities = sources[o].getCompliant_commodities().size();
         if (nb_commodities == 0) {
           System.out
               .println("[Warning] In Computation of the gradient by finite diff. 0 commodities");
           continue;
         }
 
         for (int c = 0; c < nb_commodities; c++) {
           average += init_gradient[k * C + index + c];
         }
         average /= nb_commodities;
 
         for (int c = 0; c < nb_commodities; c++)
           gradient_f[k * C + index + c] = init_gradient[k * C + index + c]
               - average;
         index += nb_commodities;
       }
     }
   }
 }
