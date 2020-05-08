 /*
  * Redberry: symbolic tensor computations.
  *
  * Copyright (c) 2010-2012:
  *   Stanislav Poslavsky   <stvlpos@mail.ru>
  *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
  *
  * This file is part of Redberry.
  *
  * Redberry is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Redberry is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
  */
 package cc.redberry.physics.kv;
 
 import cc.redberry.core.context.CC;
 import cc.redberry.core.tensor.Expression;
 import cc.redberry.core.tensor.Tensor;
 import cc.redberry.core.tensor.testing.TTest;
 import cc.redberry.core.transformations.RenameConflictingIndices;
 import cc.redberry.core.utils.Indicator;
 import cc.redberry.transformation.*;
 import cc.redberry.transformation.collect.CollectFactory;
 import cc.redberry.transformation.collect.CollectPowers;
 import cc.redberry.transformation.concurrent.EACScalars;
 import cc.redberry.transformation.contractions.IndicesContractionsTransformation;
 import cc.redberry.transformation.substitutions.TensorTreeIndicatorImpl;
 import static cc.redberry.physics.util.IndicesFactoryUtil.createIndices;
 import static cc.redberry.physics.util.IndicesFactoryUtil.doubleAndDumpIndices;
 
 
 /**
  *
  * @author Dmitry Bolotin
  * @author Stanislav Poslavsky
  * @author Konstantin Kiselev
  */
 public class OneLoop {
     /*
      * General definitions of auxilary tensors
      */
     public final Expression L = new Expression("L = 2");
     public final Expression P =
             new Expression("P^{\\alpha\\beta}_{\\mu\\nu} = (1/2)*(d^{\\alpha}_{\\mu}*d^{\\beta}_{\\nu}+d^{\\alpha}_{\\nu}*d^{\\beta}_{\\mu})-"
             + "(1/4)*g_{\\mu\\nu}*g^{\\alpha\\beta}");
     public static final Expression KRONECKER_DIMENSION =
             new Expression("d^{\\alpha}_{\\alpha} = 4");
     /*
      * Additional input
      */
     public final Expression RICCI =
             new Expression("R_{\\mu\\nu} = -g_{\\mu\\nu}*LAMBDA");
     public final Expression RIMAN =
            new Expression("R_{\\mu\\nu\\alpha\\beta} = (1/3)*(g_{\\mu\\beta}*g_{\\nu\\alpha}-g_{\\mu\\alpha}*g_{\\nu\\beta})*LAMBDA");
     /*
      * Effective action section
      */
     public final Expression ACTION =
             new Expression("ACTION = Flat + WR + SR + SSR + FF + FR + RR ");
     public final Expression RR =
             new Expression("RR = (1/10)*L*L*HATK^{\\delta}*DELTA^{\\mu\\nu\\alpha\\beta}*HATK^{\\gamma}*n_{\\sigma}*n_{\\lambda}*R^{\\sigma}_{\\alpha\\beta\\gamma}*R^{\\lambda}_{\\mu\\nu\\delta} + "
             + "L*L*(L-1)*HATK^{\\delta}*DELTA^{\\alpha\\beta\\gamma}*HATK^{\\mu\\nu}*n_{\\sigma}*n_{\\lambda}*(-(1/10)*R^{\\lambda}_{\\mu\\gamma\\nu}*R^{\\sigma}_{\\alpha\\delta\\beta}+(1/15)*R^{\\lambda}_{\\delta\\alpha\\nu}*R^{\\sigma}_{\\beta\\mu\\gamma}+(1/60)*R^{\\lambda}_{\\beta\\delta\\nu}*R^{\\sigma}_{\\gamma\\mu\\alpha})+"
             + "L*L*(L-1)*(L-1)*HATK^{\\gamma\\delta}*DELTA^{\\alpha\\beta}*HATK^{\\mu\\nu}*n_{\\sigma}*n_{\\lambda}*(-(1/20)*R^{\\lambda}_{\\mu\\beta\\nu}*R^{\\sigma}_{\\delta\\alpha\\gamma}+(1/180)*R^{\\lambda}_{\\alpha\\nu\\beta}*R^{\\sigma}_{\\gamma\\delta\\mu}-(7/360)*R^{\\lambda}_{\\mu\\gamma\\nu}*R^{\\sigma}_{\\alpha\\delta\\beta}-(1/240)*R^{\\lambda}_{\\delta\\beta\\nu}*R^{\\sigma}_{\\gamma\\alpha\\mu}-(1/120)*R^{\\lambda}_{\\beta\\gamma\\nu}*R^{\\sigma}_{\\alpha\\delta\\mu}-(1/30)*R^{\\lambda}_{\\delta\\beta\\nu}*R^{\\sigma}_{\\alpha\\gamma\\mu})+"
             + "L*L*(L-1)*HATK^{\\mu\\nu}*DELTA^{\\alpha\\beta\\gamma}*HATK^{\\delta}*n_{\\sigma}*n_{\\lambda}*((7/120)*R^{\\lambda}_{\\beta\\gamma\\nu}*R^{\\sigma}_{\\mu\\alpha\\delta}-(3/40)*R^{\\lambda}_{\\beta\\gamma\\delta}*R^{\\sigma}_{\\mu\\alpha\\nu}+(1/120)*R^{\\lambda}_{\\delta\\gamma\\nu}*R^{\\sigma}_{\\alpha\\beta\\mu})+"
             + "L*L*HATK^{\\mu}*DELTA^{\\alpha\\beta\\gamma}*HATK^{\\nu}*n_{\\lambda}*(-(1/8)*R_{\\beta\\gamma}*R^{\\lambda}_{\\nu\\alpha\\mu}+(3/20)*R_{\\beta\\gamma}*R^{\\lambda}_{\\mu\\alpha\\nu}+(3/40)*R_{\\alpha\\mu}*R^{\\lambda}_{\\beta\\gamma\\nu}+(1/40)*R^{\\sigma}_{\\beta\\gamma\\mu}*R^{\\lambda}_{\\nu\\alpha\\sigma}-(3/20)*R^{\\sigma}_{\\alpha\\beta\\mu}*R^{\\lambda}_{\\gamma\\nu\\sigma}+(1/10)*R^{\\sigma}_{\\alpha\\beta\\nu}*R^{\\lambda}_{\\gamma\\mu\\sigma})+"
             + "L*L*(L-1)*HATK^{\\gamma}*DELTA^{\\alpha\\beta}*HATK^{\\mu\\nu}*n_{\\lambda}*((1/20)*R_{\\alpha\\nu}*R^{\\lambda}_{\\gamma\\beta\\mu}+(1/20)*R_{\\alpha\\gamma}*R^{\\lambda}_{\\mu\\beta\\nu}+(1/10)*R_{\\alpha\\beta}*R^{\\lambda}_{\\mu\\gamma\\nu}+(1/20)*R^{\\sigma}_{\\alpha\\nu\\gamma}*R^{\\lambda}_{\\sigma\\beta\\mu}-(1/60)*R^{\\sigma}_{\\mu\\alpha\\nu}*R^{\\lambda}_{\\beta\\sigma\\gamma}+(1/10)*R^{\\sigma}_{\\alpha\\beta\\gamma}*R^{\\lambda}_{\\mu\\sigma\\nu}-(1/12)*R^{\\sigma}_{\\alpha\\beta\\nu}*R^{\\lambda}_{\\mu\\sigma\\gamma})+"
             + "L*L*(L-1)*(L-1)*HATK^{\\alpha\\beta}*DELTA^{\\gamma}*HATK^{\\mu\\nu}*n_{\\lambda}*((1/60)*R_{\\alpha\\mu}*R^{\\lambda}_{\\beta\\nu\\gamma}-(1/20)*R_{\\alpha\\mu}*R^{\\lambda}_{\\gamma\\nu\\beta}+(1/120)*R_{\\alpha\\beta}*R^{\\lambda}_{\\mu\\nu\\gamma}+(3/40)*R_{\\alpha\\gamma}*R^{\\lambda}_{\\nu\\beta\\mu}+(1/20)*R^{\\sigma}_{\\gamma\\mu\\alpha}*R^{\\lambda}_{\\nu\\sigma\\beta}+(1/120)*R^{\\sigma}_{\\alpha\\mu\\gamma}*R^{\\lambda}_{\\beta\\nu\\sigma}-(1/40)*R^{\\sigma}_{\\alpha\\mu\\gamma}*R^{\\lambda}_{\\sigma\\nu\\beta}+(1/40)*R^{\\sigma}_{\\alpha\\mu\\beta}*R^{\\lambda}_{\\sigma\\nu\\gamma}-(1/20)*R^{\\sigma}_{\\alpha\\mu\\beta}*R^{\\lambda}_{\\gamma\\nu\\sigma}-(1/40)*R^{\\sigma}_{\\mu\\beta\\nu}*R^{\\lambda}_{\\gamma\\sigma\\alpha})+"
             + "L*L*(L-1)*HATK^{\\alpha\\beta}*DELTA^{\\mu\\nu}*HATK^{\\gamma}*n_{\\lambda}*((1/20)*R^{\\sigma}_{\\mu\\nu\\beta}*R^{\\lambda}_{\\gamma\\sigma\\alpha}-(7/60)*R^{\\sigma}_{\\beta\\mu\\alpha}*R^{\\lambda}_{\\gamma\\nu\\sigma}+(1/20)*R^{\\sigma}_{\\beta\\mu\\alpha}*R^{\\lambda}_{\\sigma\\nu\\gamma}+(1/10)*R^{\\sigma}_{\\mu\\beta\\gamma}*R^{\\lambda}_{\\nu\\alpha\\sigma}+(1/60)*R^{\\sigma}_{\\mu\\beta\\gamma}*R^{\\lambda}_{\\alpha\\nu\\sigma}+(7/120)*R_{\\alpha\\beta}*R^{\\lambda}_{\\nu\\gamma\\mu}+(11/60)*R_{\\beta\\mu}*R^{\\lambda}_{\\nu\\alpha\\gamma})");
     public final Expression[] TERMs = new Expression[]{RR};
     /*
      *Section for defining \Delta's 
      */
     public final Expression DELTA_1 =
             new Expression("DELTA^{\\mu} = -L*HATK^{\\mu}");
     public final Expression DELTA_2 =
             new Expression("DELTA^{\\mu\\nu} =-(1/2)*L*(L-1)*HATK^{\\mu\\nu}+L*L*HATK^{\\mu}*HATK^{\\nu}+L*L*HATK^{\\nu}*HATK^{\\mu}");
     public final Expression DELTA_3 =
             new Expression("DELTA^{\\mu\\nu\\alpha}="
             + "(1/6)*L*L*(L-1)*(HATK^{\\mu\\nu}*HATK^{\\alpha}+HATK^{\\mu\\alpha}*HATK^{\\nu}+HATK^{\\alpha\\nu}*HATK^{\\mu})+"
             + "(1/6)*L*L*(L-1)*(HATK^{\\alpha}*HATK^{\\mu\\nu}+HATK^{\\nu}*HATK^{\\mu\\alpha}+HATK^{\\mu}*HATK^{\\alpha\\nu})"
             + "-(1/6)*L*L*L*(HATK^{\\mu}*HATK^{\\nu}*HATK^{\\alpha}+HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\nu}+HATK^{\\nu}*HATK^{\\mu}*HATK^{\\alpha}+HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\mu}+HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\nu}+HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\mu})");
     public final Expression DELTA_4 =
             new Expression("DELTA^{\\mu\\nu\\alpha\\beta}="
             + "(1/24)*L*L*(L-1)*(L-1)*("
             + "HATK^{\\mu\\nu}*HATK^{\\alpha\\beta}+"
             + "HATK^{\\mu\\alpha}*HATK^{\\nu\\beta}+"
             + "HATK^{\\mu\\beta}*HATK^{\\alpha\\nu}+"
             + "HATK^{\\nu\\alpha}*HATK^{\\mu\\beta}+"
             + "HATK^{\\nu\\beta}*HATK^{\\mu\\alpha}+"
             + "HATK^{\\alpha\\beta}*HATK^{\\mu\\nu})"
             + "-(1/24)*L*L*L*(L-1)*("
             + "HATK^{\\mu\\nu}*HATK^{\\alpha}*HATK^{\\beta}+"
             + "HATK^{\\mu\\nu}*HATK^{\\beta}*HATK^{\\alpha}+"
             + "HATK^{\\mu\\alpha}*HATK^{\\nu}*HATK^{\\beta}+"
             + "HATK^{\\mu\\alpha}*HATK^{\\beta}*HATK^{\\nu}+"
             + "HATK^{\\mu\\beta}*HATK^{\\alpha}*HATK^{\\nu}+"
             + "HATK^{\\mu\\beta}*HATK^{\\nu}*HATK^{\\alpha}+"
             + "HATK^{\\nu\\alpha}*HATK^{\\mu}*HATK^{\\beta}+"
             + "HATK^{\\nu\\alpha}*HATK^{\\beta}*HATK^{\\mu}+"
             + "HATK^{\\nu\\beta}*HATK^{\\mu}*HATK^{\\alpha}+"
             + "HATK^{\\nu\\beta}*HATK^{\\alpha}*HATK^{\\mu}+"
             + "HATK^{\\alpha\\beta}*HATK^{\\nu}*HATK^{\\mu}+"
             + "HATK^{\\alpha\\beta}*HATK^{\\mu}*HATK^{\\nu})"
             + "-(1/24)*L*L*L*(L-1)*("
             + "HATK^{\\alpha}*HATK^{\\mu\\nu}*HATK^{\\beta}+"
             + "HATK^{\\beta}*HATK^{\\mu\\nu}*HATK^{\\alpha}+"
             + "HATK^{\\nu}*HATK^{\\mu\\alpha}*HATK^{\\beta}+"
             + "HATK^{\\beta}*HATK^{\\mu\\alpha}*HATK^{\\nu}+"
             + "HATK^{\\alpha}*HATK^{\\mu\\beta}*HATK^{\\nu}+"
             + "HATK^{\\nu}*HATK^{\\mu\\beta}*HATK^{\\alpha}+"
             + "HATK^{\\mu}*HATK^{\\nu\\alpha}*HATK^{\\beta}+"
             + "HATK^{\\beta}*HATK^{\\nu\\alpha}*HATK^{\\mu}+"
             + "HATK^{\\mu}*HATK^{\\nu\\beta}*HATK^{\\alpha}+"
             + "HATK^{\\alpha}*HATK^{\\nu\\beta}*HATK^{\\mu}+"
             + "HATK^{\\nu}*HATK^{\\alpha\\beta}*HATK^{\\mu}+"
             + "HATK^{\\mu}*HATK^{\\alpha\\beta}*HATK^{\\nu})"
             + "-(1/24)*L*L*L*(L-1)*("
             + "HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\mu\\nu}+"
             + "HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\mu\\nu}+"
             + "HATK^{\\nu}*HATK^{\\beta}*HATK^{\\mu\\alpha}+"
             + "HATK^{\\beta}*HATK^{\\nu}*HATK^{\\mu\\alpha}+"
             + "HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\mu\\beta}+"
             + "HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\mu\\beta}+"
             + "HATK^{\\mu}*HATK^{\\beta}*HATK^{\\nu\\alpha}+"
             + "HATK^{\\beta}*HATK^{\\mu}*HATK^{\\nu\\alpha}+"
             + "HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\nu\\beta}+"
             + "HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\nu\\beta}+"
             + "HATK^{\\nu}*HATK^{\\mu}*HATK^{\\alpha\\beta}+"
             + "HATK^{\\mu}*HATK^{\\nu}*HATK^{\\alpha\\beta})"
             + "+(1/24)*L*L*L*L*("
             + "HATK^{\\mu}*HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\beta}+"
             + "HATK^{\\mu}*HATK^{\\nu}*HATK^{\\beta}*HATK^{\\alpha}+"
             + "HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\beta}+"
             + "HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\nu}+"
             + "HATK^{\\mu}*HATK^{\\beta}*HATK^{\\nu}*HATK^{\\alpha}+"
             + "HATK^{\\mu}*HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\nu}+"
             + "HATK^{\\nu}*HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\beta}+"
             + "HATK^{\\nu}*HATK^{\\mu}*HATK^{\\beta}*HATK^{\\alpha}+"
             + "HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\beta}+"
             + "HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\mu}+"
             + "HATK^{\\nu}*HATK^{\\beta}*HATK^{\\mu}*HATK^{\\alpha}+"
             + "HATK^{\\nu}*HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\mu}+"
             + "HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\mu}*HATK^{\\beta}+"
             + "HATK^{\\alpha}*HATK^{\\nu}*HATK^{\\beta}*HATK^{\\mu}+"
             + "HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\nu}*HATK^{\\beta}+"
             + "HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\beta}*HATK^{\\nu}+"
             + "HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\mu}*HATK^{\\nu}+"
             + "HATK^{\\alpha}*HATK^{\\beta}*HATK^{\\nu}*HATK^{\\mu}+"
             + "HATK^{\\beta}*HATK^{\\nu}*HATK^{\\mu}*HATK^{\\alpha}+"
             + "HATK^{\\beta}*HATK^{\\nu}*HATK^{\\alpha}*HATK^{\\mu}+"
             + "HATK^{\\beta}*HATK^{\\mu}*HATK^{\\nu}*HATK^{\\alpha}+"
             + "HATK^{\\beta}*HATK^{\\mu}*HATK^{\\alpha}*HATK^{\\nu}+"
             + "HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\nu}+"
             + "HATK^{\\beta}*HATK^{\\alpha}*HATK^{\\mu}*HATK^{\\nu})");
     public final Expression[] DELTAs = new Expression[]{DELTA_1, DELTA_2, DELTA_3, DELTA_4};
     /*
      *Section for defining \hat K's 
      */
     public final Expression HATK_1 =
             new Expression("HATK^{\\mu} = KINV*K^{\\mu\\nu}*n_{\\nu}");
     public final Expression HATK_2 =
             new Expression("HATK^{\\mu\\nu} = KINV*K^{\\mu\\nu}");
     public final Expression HATK_3 =
             new Expression("HATK^{\\mu\\nu\\alpha} = HATK^{\\mu\\nu\\alpha}");
     public final Expression HATK_4 =
             new Expression("HATK^{\\mu\\nu\\alpha\\beta} = HATK^{\\mu\\nu\\alpha\\beta}");
     public final Expression[] HATKs = new Expression[]{HATK_1, HATK_2, HATK_3, HATK_4};
     public final Expression[] ALL = new Expression[]{RR, DELTA_1, DELTA_2, DELTA_3, DELTA_4, HATK_1, HATK_2, HATK_3, HATK_4};
     /*
      * The indices ^{\\alpha\\beta}_{\\gamma\\delta} are the matrix indices
      */
     public final Expression MATRIX_K =
             new Expression("K^{\\mu\\nu}^{\\alpha\\beta}_{\\gamma\\delta} = g^{\\mu\\nu}*P^{\\alpha\\beta}_{\\gamma\\delta}+"
             + "(1+2*beta)*((1/4)*(d^{\\mu}_{\\gamma}*g^{\\alpha \\nu}*d^{\\beta}_{\\delta} + d^{\\mu}_{\\delta}*g^{\\alpha \\nu}*d^{\\beta}_{\\gamma}+d^{\\mu}_{\\gamma}*g^{\\beta \\nu}*d^{\\alpha}_{\\delta}+ d^{\\mu}_{\\delta}*g^{\\beta \\nu}*d^{\\alpha}_{\\gamma})+"
             + "(1/4)*(d^{\\nu}_{\\gamma}*g^{\\alpha \\mu}*d^{\\beta}_{\\delta} + d^{\\nu}_{\\delta}*g^{\\alpha \\mu}*d^{\\beta}_{\\gamma}+d^{\\nu}_{\\gamma}*g^{\\beta \\mu}*d^{\\alpha}_{\\delta}+ d^{\\nu}_{\\delta}*g^{\\beta \\mu}*d^{\\alpha}_{\\gamma}) -"
             + "(1/4)*(g_{\\gamma\\delta}*g^{\\mu \\alpha}*g^{\\nu \\beta}+g_{\\gamma\\delta}*g^{\\mu \\beta}*g^{\\nu \\alpha})-"
             + "(1/4)*(g^{\\alpha\\beta}*d^{\\mu}_{\\gamma}*d^{\\nu}_{\\delta}+g^{\\alpha\\beta}*d^{\\mu}_{\\delta}*d^{\\nu}_{\\gamma})+(1/8)*g^{\\mu\\nu}*g_{\\gamma\\delta}*g^{\\alpha\\beta})");
     /*
      * The indices ^{\\alpha\\beta}_{\\mu\\nu} are the matrix indices
      */
     public final Expression MATRIX_K_INV =
             new Expression("KINV^{\\alpha\\beta}_{\\mu\\nu} = P^{\\alpha\\beta}_{\\mu\\nu}+a*g_{\\mu\\nu}*g^{\\alpha\\beta}+"
             + "(1/4)*b*(n_{\\mu}*n^{\\alpha}*d^{\\beta}_{\\nu}+n_{\\mu}*n^{\\beta}*d^{\\alpha}_{\\nu}+n_{\\nu}*n^{\\alpha}*d^{\\beta}_{\\mu})+n_{\\nu}*n^{\\beta}*d^{\\alpha}_{\\mu}+"
             + "c*(n_{\\mu}*n_{\\nu}*g^{\\alpha\\beta}+n^{\\alpha}*n^{\\beta}*g_{\\mu\\nu})+d*n_{\\mu}*n_{\\nu}*n^{\\alpha}*n^{\\beta}");
     /*
      * Collecting all matrices in expressions together
      */
     public static final Tensor[] MATRICES = new Tensor[]{
         CC.parse("K^{\\mu\\nu}"),
         CC.parse("KINV"),
         CC.parse("HATK"),
         CC.parse("HATK^{\\mu}"),
         CC.parse("HATK^{\\mu\\nu}"),
         CC.parse("HATK^{\\mu\\nu\\alpha}"),
         CC.parse("DELTA^{\\mu}"),
         CC.parse("DELTA^{\\mu\\nu}"),
         CC.parse("DELTA^{\\mu\\nu\\alpha}"),
         CC.parse("DELTA^{\\mu\\nu\\alpha\\beta}")};
 
     public static enum EVAL {
         INITIALIZE,
         EVAL_HATK,
         EVAL_HATK_DELTA,
         EVAL_ALL
     }
 
     public OneLoop() {
         for (Expression ex : ALL)
             ex.eval(new Transformer(RenameConflictingIndices.INSTANCE));
     }
 
     public void substituteL() {
         for (Expression ex : ALL)
             ex.eval(L.asSubstitution(), CalculateNumbers.INSTANCE);
     }
     private boolean indicesInserted = false;
 
     public void insertIndices() {
         if (indicesInserted)
             throw new IllegalAccessError("Indices are already inserted");
         Transformation indicesInsertion;
         indicesInsertion = new IndicesInsertion(matricesIndicator, createIndices(HATKs, "^{\\mu\\nu}_{\\alpha\\beta}"));
         for (Expression hatK : HATKs)
             hatK.eval(indicesInsertion);
         indicesInsertion = new IndicesInsertion(matricesIndicator, createIndices(DELTAs, "^{\\mu\\nu}_{\\alpha\\beta}"));
         for (Expression delta : DELTAs)
             delta.eval(indicesInsertion);
         indicesInsertion = new IndicesInsertion(matricesIndicator, doubleAndDumpIndices(createIndices(TERMs, "^{\\mu\\nu}")));
         for (Expression term : TERMs)
             term.eval(indicesInsertion);
         indicesInserted = true;
     }
 
     public final void evalHatK() {
         for (Expression hatK : HATKs)
             hatK.eval(
                     MATRIX_K.asSubstitution(),
                     MATRIX_K_INV.asSubstitution(),
                     P.asSubstitution(),
                     new Transformer(ExpandBrackets.EXPAND_EXCEPT_SYMBOLS),
                     IndicesContractionsTransformation.CONTRACTIONS_WITH_METRIC,
                     KRONECKER_DIMENSION.asSubstitution(),
                     CollectFactory.createCollectEqualTerms(),
                     CalculateNumbers.INSTANCE,
                     EACScalars.getTransformer(),
                     CalculateNumbers.INSTANCE);
     }
 
     public OneLoop(EVAL eval) {
         switch (eval) {
             case INITIALIZE:
                 break;
             case EVAL_HATK:
                 evalHatK();
                 break;
             case EVAL_HATK_DELTA:
                 evalHatK();
                 evalHatKDelta();
                 break;
             case EVAL_ALL:
                 evalRR();
                 evalDeltas();
                 for (Expression delta : DELTAs)
                     RR.eval(delta.asSubstitution());
                 RR.eval(
                         IndicesContractionsTransformation.CONTRACTIONS_WITH_METRIC,
                         KRONECKER_DIMENSION.asSubstitution(),
                         CalculateNumbers.INSTANCE,
                         new Transformer(RenameConflictingIndices.INSTANCE),
                         new Transformer(ExpandBrackets.EXPAND_EXCEPT_SYMBOLS),
                         CollectFactory.createCollectEqualTerms(),
                         CalculateNumbers.INSTANCE);
                 evalHatK();
                 for (Expression hatK : HATKs)
                     RR.eval(hatK.asSubstitution());
                 RR.eval(
                         IndicesContractionsTransformation.CONTRACTIONS_WITH_METRIC,
                         KRONECKER_DIMENSION.asSubstitution(),
                         CalculateNumbers.INSTANCE,
                         new Transformer(RenameConflictingIndices.INSTANCE),
                         new Transformer(ExpandBrackets.EXPAND_EXCEPT_SYMBOLS),
                         CollectFactory.createCollectEqualTerms(),
                         CalculateNumbers.INSTANCE);
         }
     }
 
     public final void evalHatKDelta() {
         Transformation indicesInsertion;
         indicesInsertion = new IndicesInsertion(matricesIndicator, createIndices(DELTAs, "^{\\mu\\nu}_{\\alpha\\beta}"));
         for (Expression delta : DELTAs)
             delta.eval(
                     indicesInsertion,
                     L.asSubstitution(),
                     CalculateNumbers.INSTANCE,
                     HATK_1.asSubstitution(),
                     HATK_2.asSubstitution(),
                     HATK_3.asSubstitution(),
                     HATK_4.asSubstitution(),
                     new Transformer(ExpandBrackets.EXPAND_EXCEPT_SYMBOLS),
                     IndicesContractionsTransformation.CONTRACTIONS_WITH_METRIC,
                     KRONECKER_DIMENSION.asSubstitution(),
                     CollectFactory.createCollectEqualTerms(),
                     CalculateNumbers.INSTANCE,
                     CollectFactory.createCollectAllScalars(),
                     CalculateNumbers.INSTANCE);
     }
 
     public final void evalDeltas() {
         Transformation indicesInsertion;
         indicesInsertion = new IndicesInsertion(matricesIndicator, createIndices(DELTAs, "^{\\mu\\nu}_{\\alpha\\beta}"));
         for (Expression delta : DELTAs)
             delta.eval(
                     indicesInsertion,
                     L.asSubstitution(),
                     CalculateNumbers.INSTANCE,
                     new Transformer(RenameConflictingIndices.INSTANCE),
                     new Transformer(ExpandBrackets.EXPAND_EXCEPT_SYMBOLS),
                     //                    IndicesContractionsTransformation.CONTRACTIONS_WITH_METRIC,
                     KRONECKER_DIMENSION.asSubstitution(),
                     //                    CollectFactory.createCollectEqualTerms1(),
                     CalculateNumbers.INSTANCE //                    ,
                     //                    CollectFactory.createCollectAllScalars(),
                     //                    CalculateNumbers.INSTANCE
                     );
     }
 
     public final void evalRR() {
         Transformation indicesInsertion;
         indicesInsertion = new IndicesInsertion(matricesIndicator, doubleAndDumpIndices(createIndices(TERMs, "^{\\mu\\nu}")));
         RR.eval(
                 indicesInsertion,
                 L.asSubstitution(),
                 CalculateNumbers.INSTANCE,
                 RICCI.asSubstitution(),
                 RIMAN.asSubstitution(),
                 new Transformer(RenameConflictingIndices.INSTANCE),
                 new Transformer(ExpandBrackets.EXPAND_EXCEPT_SYMBOLS),
                 IndicesContractionsTransformation.CONTRACTIONS_WITH_METRIC,
                 KRONECKER_DIMENSION.asSubstitution(),
                 CalculateNumbers.INSTANCE,
                 new Transformer(CollectPowers.INSTANCE),
                 CollectFactory.createCollectEqualTerms());
 
     }
     public final Indicator<Tensor> matricesIndicator = new TensorTreeIndicatorImpl(new Indicator<Tensor>() {
         @Override
         public boolean is(Tensor tensor) {
             for (Tensor m : MATRICES)
                 if (TTest.testEqualstensorStructure(tensor, m))
                     return true;
             return false;
         }
     });
 }
