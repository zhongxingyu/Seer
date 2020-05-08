 package VRP_ILS;
 
 import VRP.MetaheuristicOptimizationAlgorithm;
 import VRP.VehicleRoutingProblem;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Andrea Aranguren
  * @author Simon Rojas
  */
 public class IteratedLocalSearchAlgorithm
         implements MetaheuristicOptimizationAlgorithm {
 
     //INICIO de estructuras para representar una solucion al problema
     Integer[] customers;
     List<List<Integer>> currentRoutes;
     List<Double> costOfRoutes;
     List<Integer> routeDemands;
     double totalCost;
     double totalDistance;
     List<List<Integer>> bestRoutes;
     double bestTotalCost;
     double bestTotalDistance;
     //FIN de estructuras para representar una solución al problema
     //INICIO de parametros configurables por el usuario
     int maxIter = 2;
     int localSearchMaxIter = 3;
     //FIN de parametros configurables por el usuario
     VehicleRoutingProblem vrpInstance;
     int numberOfCustomers;
     private static final double mili = 1000000000.0;
 
     //TODO solo para pruebas
     private double calculateRouteDistance(List<List<Integer>> routes) {
         double distance = 0;
         double routeDistance;
 
         for (int j = 0; j < routes.size(); j++) {
             List<Integer> route = routes.get(j);
             routeDistance = 0;
             distance += vrpInstance.getCost(0, route.get(0));
             routeDistance += vrpInstance.getCost(0, route.get(0));
             for (int i = 1; i < route.size(); i++) {
                 distance += vrpInstance.getCost(route.get(i - 1), route.get(i));
                 routeDistance += vrpInstance.getCost(route.get(i - 1), route.get(i));
             }
             distance += vrpInstance.getCost(route.get(route.size() - 1), 0);
             routeDistance += vrpInstance.getCost(route.get(route.size() - 1), 0);
             costOfRoutes.set(j, routeDistance);
 
         }
         return distance;
     }
 
     public IteratedLocalSearchAlgorithm(VehicleRoutingProblem vrpInstance) {
         numberOfCustomers = vrpInstance.getNumberOfCustomers();
         customers = new Integer[numberOfCustomers + 1];
         currentRoutes = new ArrayList<List<Integer>>(numberOfCustomers);
         bestRoutes = new ArrayList<List<Integer>>(numberOfCustomers);
         for (int i = 0; i < numberOfCustomers; i++) {
             currentRoutes.add(new ArrayList<Integer>(numberOfCustomers));
         }
         for (int i = 0; i < numberOfCustomers + 1; i++) {
             customers[i] = new Integer(i);
         }
         costOfRoutes = new ArrayList<Double>(numberOfCustomers);
         routeDemands = new ArrayList<Integer>(numberOfCustomers);
         this.vrpInstance = vrpInstance;
         this.totalCost = this.vrpInstance.getDropTime() * numberOfCustomers;
         this.totalDistance = 0;
         constructInitialSolution();
     }
 
     private void constructInitialSolution() {
         initializePartition();
         int n = vrpInstance.getNumberOfCustomers();
         double s[][] = new double[n][n];
         calculateSavings(s);
         Index index = getBestSaving(s);
         while (index.getSaving() >= 0) {
             mergeRoutes(index);
             index = getBestSaving(s);
         }
         updateBestRoutes();
         this.totalDistance = calculateRouteDistance(bestRoutes);
         this.totalCost = this.totalDistance
                 + (numberOfCustomers * vrpInstance.getDropTime());
         bestTotalCost = totalCost;
         bestTotalDistance = totalDistance;
         boolean valid = validateResult();
         //TODO Borrar estas impresiones
         System.out.println("Es valida la solucion inicial: " + valid);
         System.out.println("Distancia de la solucion inicial: " + totalDistance);
         System.out.println("Distancia calculada completa: " + calculateRouteDistance(currentRoutes));
         System.out.println("RUTAS INICIALES: ");
         System.out.println(this.routesToString());
     }
 
     private void mergeRoutes(Index index) {
         int i = getIndexOfRouteI(index.i);
         int j = getIndexOfRouteJ(index.j);
         if (i != -1 && j != -1) {
             double costIJ = costOfRoutes.get(i) + costOfRoutes.get(j)
                     + ((currentRoutes.get(i).size() + currentRoutes.get(j).size())
                     * vrpInstance.getDropTime()) - index.saving;
             int demandIJ = this.routeDemands.get(i) + this.routeDemands.get(j);
             if (costIJ < vrpInstance.getMaximumRouteTime()
                     && demandIJ <= vrpInstance.getVehicleCapacity() && i != j) {
 
                 doMerge(i, j, index.saving);
             }
         }
     }
 
     @Override
     public ILSSolutionSet execute() {
         int iteration = 0;
         int bestIteration = 0;
         long tIni;
         long tFin;
         long tFinBest;
         boolean accepted;
 
         tIni = System.nanoTime();
         tFinBest = System.nanoTime();
         while (iteration < this.maxIter) {
             localSearch();
             accepted = acceptanceCriterion();
             if (accepted) {
                 tFinBest = System.nanoTime();
                 bestIteration = iteration;
             }
             perturbate();
             iteration += 1;
         }
         tFin = System.nanoTime();
         double tBest = (tFinBest - tIni) / mili;
         double tTotal = (tFin - tIni) / mili;
         String finalRoutes = routesToString();
         //TODO borrar estas otras impresiones
         System.out.println("Ruta final es valida: " + validateResult());
         System.out.println("RUTAS FINALES: ");
         System.out.println(this.routesToString());
         double bestDistance = calculateRouteDistance(bestRoutes);
         //TODO Borrar
         System.out.println("Distancia de la mejor solucion recalculada: " + bestDistance);
         return (new ILSSolutionSet(this.bestTotalDistance,
                 bestIteration, tBest, tTotal, bestRoutes.size(), iteration,
                 finalRoutes, this.bestTotalCost));
     }
 
     private void updateBestRoutes() {
         bestRoutes.clear();
         for (int i = 0; i < currentRoutes.size(); i++) {
             bestRoutes.add(new ArrayList<Integer>(
                     currentRoutes.get(i).size()));
             for (int j = 0; j < currentRoutes.get(i).size(); j++) {
                 bestRoutes.get(i).add(currentRoutes.get(i).get(j));
             }
         }
     }
 
     private void resetRoutes() {
         currentRoutes.clear();
         for (int i = 0; i < bestRoutes.size(); i++) {
             currentRoutes.add(new ArrayList<Integer>(bestRoutes.get(i).size()));
             for (int j = 0; j < bestRoutes.get(i).size(); j++) {
                 currentRoutes.get(i).add(bestRoutes.get(i).get(j));
             }
         }
     }
 
     private void initializePartition() {
         int i = 0;
         double cost;
         for (List<Integer> route : this.currentRoutes) {
             route.add(this.customers[i + 1]);
             cost = vrpInstance.getCost(0, i + 1) * 2;
             this.costOfRoutes.add(i, new Double(cost));
             this.routeDemands.add(new Integer(vrpInstance.getCustomerDemand(i + 1)));
             this.totalDistance += cost;
             this.totalCost += cost;
             i++;
         }
     }
 
     private void calculateSavings(double[][] s) {
         for (int i = 0; i < s.length; i++) {
             for (int j = i + 1; j < s.length; j++) {
                 double a = vrpInstance.getCost(i + 1, 0);
                 double b = vrpInstance.getCost(0, j + 1);
                 double c = vrpInstance.getCost(i + 1, j + 1);
                 s[i][j] = (a + b) - c;
                 //Esto sólo se hace por completitud ya que no se utilizará
                 s[j][i] = (a + b) - c;
             }
         }
     }
 
     private Index getBestSaving(double[][] s) {
         int x = -1;
         int y = -1;
         double max = -1;
         for (int i = 0; i < s.length; i++) {
             for (int j = 0; j < s.length && i != j; j++) {
                 if (s[i][j] > max) {
                     max = s[i][j];
                     x = i;
                     y = j;
                 }
             }
         }
         if (x != -1 && y != -1) {
             s[x][y] = -1;
             s[y][x] = -1;
         }
         return (new Index(x + 1, y + 1, max));
     }
 
     private int getIndexOfRouteI(int elem) {
         Integer element = new Integer(elem);
         int i = 0;
         for (List<Integer> route : currentRoutes) {
             if (route.indexOf(element) == 0) {
                 return (i);
             }
             i++;
         }
         return (-1);
     }
 
     private int getIndexOfRouteJ(int elem) {
         Integer element = new Integer(elem);
         int i = 0;
         for (List<Integer> route : currentRoutes) {
             if (route.indexOf(element) == (route.size() - 1)) {
                 return (i);
             }
             i++;
         }
         return (-1);
     }
 
     private void doMerge(int i, int j, double saving) {
         this.currentRoutes.get(i).addAll(this.currentRoutes.get(j));
         double newCost =
                 this.costOfRoutes.get(i) + this.costOfRoutes.get(j) - saving;
         this.costOfRoutes.set(i, newCost);
         int newDemand = this.routeDemands.get(i) + this.routeDemands.get(j);
         this.routeDemands.set(i, newDemand);
         this.currentRoutes.remove(j);
         this.routeDemands.remove(j);
         this.costOfRoutes.remove(j);
         this.totalCost = this.totalCost - saving;
         this.totalDistance = this.totalDistance - saving;
     }
 
     private void printResult() {
         int i = 0;
         for (List<Integer> route : bestRoutes) {
             System.out.println("0" + route.toString() + "0");
             System.out.println("Costo ruta: " + costOfRoutes.get(i));
             i++;
         }
         System.out.println("Costo total: " + totalCost);
     }
 
     //TODO Borrar metodo
     private boolean validateResult() {
         int n = 0;
         for (List<Integer> route : bestRoutes) {
             n += route.size();
         }
         if (n != numberOfCustomers) {
             return false;
         }
         int i = 0;
         for (Double route : this.costOfRoutes) {
             if (route + (bestRoutes.get(i).size() * vrpInstance.getDropTime())
                     >= vrpInstance.getMaximumRouteTime()) {
                 return false;
             }
             i++;
         }
 
         for (Integer route : this.routeDemands) {
             if (route > vrpInstance.getVehicleCapacity()) {
                 return false;
             }
         }
 
         int ocurrences[] = new int[numberOfCustomers];
         for (int j = 0; j < numberOfCustomers; j++) {
             ocurrences[j] = 0;
         }
 
         for (List<Integer> route : bestRoutes) {
             for (Integer element : route) {
                 ocurrences[element - 1] += 1;
             }
         }
 
         for (int j = 0; j < numberOfCustomers; j++) {
             if (ocurrences[j] != 1) {
                 return false;
             }
         }
         return true;
     }
 
     private String routesToString() {
         String s = "";
         for (List<Integer> route : bestRoutes) {
             s = s + "0 ";
             for (Integer element : route) {
                 s = s + element + " ";
             }
             s = s + "0\n\n";
         }
         return s;
     }
 
     private void localSearch() {
         int i = 0;
         while (i < localSearchMaxIter) {
             generateNeighbor();
             i += 1;
         }
     }
 
     private void generateNeighbor() {
         //Se generará un vecino a traves del metodo 2-Opt y si es mejor,
         //quedara como nueva solucion.
         int numberOfRoutes = currentRoutes.size();
         int routeIndex =
                 Math.round((float) Math.random() * (numberOfRoutes - 1));
         List<Integer> route = currentRoutes.get(routeIndex);
         int routeSize = route.size();
 
         if (routeSize == 3) {
             //Hacer swap-city ya que no se puede hacer 2-Opt
             int random = Math.round((float) Math.random());
             int index0 = (random == 0) ? 0 : 2;
             int index1 = 1;
             if (index0 > index1) {
                 int swap = index1;
                 index1 = index0;
                 index0 = swap;
             }
             //Si vale la pena, hacer el swap
             double deltaCost =
                     costVariation(index0, index1, routeSize, routeIndex);
             //TODO borrar esta impresion
             System.out.println("Delta cost 3: " + deltaCost);
             if (deltaCost < 0) {
                 Integer old0 = route.get(index0);
                 Integer old1 = route.get(index1);
                 //TODO borrar esta impresion
                 System.out.println("SWAPPING ESPECIAL: " + old0 + " " + old1);
                 route.set(index0, old1);
                 route.set(index1, old0);
                 this.totalCost += deltaCost;
                 this.totalDistance += deltaCost;
                 this.costOfRoutes.set(routeIndex, costOfRoutes.get(routeIndex)
                         + deltaCost);
 //                System.out.println("Delta cost : " + deltaCost);
             }
         } else if (routeSize >= 3) {
             //Hacer 2-Opt
             int index0 =
                     Math.round((float) Math.random() * (routeSize - 1));
             int delta = Math.round((float) Math.random() * (routeSize - 2)) + 1;
             int index1 = (index0 + delta) % routeSize;
 
             if (index0 > index1) {
                 int swap = index1;
                 index1 = index0;
                 index0 = swap;
             }
             double deltaCost =
                     costVariation(index0, index1, routeSize, routeIndex);
             System.out.println("Delta cost +: " + deltaCost);
             if (deltaCost < 0) {
                 Integer old0 = route.get(index0);
                 Integer old1 = route.get(index1);
                 //TODO borrar esta impresion
                 System.out.println("SWAPPING: " + old0 + " " + old1);
                 route.set(index0, old1);
                 route.set(index1, old0);
                 this.totalCost += deltaCost;
                 this.totalDistance += deltaCost;
                //TODO borrar
                double calculatedRouteDistance = calculateRouteCost(route);
                if (calculatedRouteDistance != costOfRoutes.get(routeIndex) + deltaCost) {
                    System.out.println("ERROR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!:");
                    System.out.println(calculatedRouteDistance + " != " + costOfRoutes.get(routeIndex));
                }

                 this.costOfRoutes.set(routeIndex, costOfRoutes.get(routeIndex) + deltaCost);
 //                System.out.println("Delta cost : " + deltaCost);
             }
         }
         //TODO borrar esta impresion
         System.out.println("Distancia actual: " + this.totalDistance);
         System.out.println("Costo actual: " + this.totalCost);
     }
 
    private double calculateRouteCost(List<Integer> route) {
        double routeDistance = 0;
        routeDistance += vrpInstance.getCost(0, route.get(0));

        for (int i = 1; i < route.size(); i++) {

            routeDistance += vrpInstance.getCost(route.get(i - 1), route.get(i));
        }

        routeDistance += vrpInstance.getCost(route.get(route.size() - 1), 0);
        return routeDistance;
    }

     private boolean acceptanceCriterion() {
         if (totalDistance < bestTotalDistance) {
             updateBestRoutes();
             bestTotalCost = totalCost;
             bestTotalDistance = totalDistance;
             return true;
         }
         return false;
     }
 
     private void perturbate() {
         //TODO implement this
     }
 
     private double costVariation(int customerIndex1, int customerIndex2,
             int routeSize, int routeIndex) {
 
         double variation = 0;
         List<Integer> route = currentRoutes.get(routeIndex);
 
         int customer0 = (customerIndex1 - 1) < 0
                 ? 0 : route.get(customerIndex1 - 1);
         int customer1 = route.get(customerIndex1);
         int customer2 = route.get(customerIndex2);
         int customer3 = (customerIndex2 + 1) == routeSize
                 ? 0 : route.get(customerIndex2 + 1);
 
         variation += vrpInstance.getCost(customer0, customer2);
         //TODO borrar estas impresiones
         System.out.println("Costo de " + customer0 + " a " + customer2 + " = " + vrpInstance.getCost(customer0, customer2));
         variation += vrpInstance.getCost(customer1, customer3);
         //TODO borrar estas impresiones
         System.out.println("Costo de " + customer1 + " a " + customer3 + " = " + vrpInstance.getCost(customer1, customer3));
         variation -= vrpInstance.getCost(customer0, customer1);
         //TODO borrar estas impresiones
         System.out.println("Costo de " + customer0 + " a " + customer1 + " = " + vrpInstance.getCost(customer0, customer1));
         variation -= vrpInstance.getCost(customer2, customer3);
         //TODO borrar estas impresiones
         System.out.println("Costo de " + customer2 + " a " + customer3 + " = " + vrpInstance.getCost(customer2, customer3));
 
         return variation;
     }
 
     private static class Index {
 
         int i;
         int j;
         double saving;
 
         public Index(int i, int j, double saving) {
             this.i = i;
             this.j = j;
             this.saving = saving;
         }
 
         public int getI() {
             return i;
         }
 
         public int getJ() {
             return j;
         }
 
         public double getSaving() {
             return saving;
         }
     }
 }
