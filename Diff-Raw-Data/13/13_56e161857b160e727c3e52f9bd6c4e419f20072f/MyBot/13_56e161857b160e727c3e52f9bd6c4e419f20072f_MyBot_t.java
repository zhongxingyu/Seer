 import java.util.*;
 
 public class MyBot {
    public static final boolean SKIP_ON_ERROR = true;
 
     private static final long TIME_DIVIDER = 1;
     private static final long TIMEOUT = 1000;
     @SuppressWarnings({"PointlessArithmeticExpression"})
     private static final long TIMESTOP = (TIMEOUT - 100)/TIME_DIVIDER;
 
     public static int turn = 0;
     private static long start;
 
     public static int[][] distances;
     public static int avgDistance;
     public static int maxDistance;
 
     public static int[] growth;
 
     public static List<List<Integer>> sortedPlanets;
 
     // The DoTurn function is where your code goes. The PlanetWars object
     // contains the state of the game, including information about all planets
     // and fleets that currently exist. Inside this function, you issue orders
     // using the pw.IssueOrder() function. For example, to send 10 ships from
     // planet 3 to planet 8, you would say pw.IssueOrder(3, 8, 10).
     //
     // There is already a basic strategy in place here. You can use it as a
     // starting point, or you can throw it out entirely and replace it with
     // your own. Check out the tutorials and articles on the contest website at
     // http://www.ai-contest.com/resources.
     public static void DoTurn(PlanetWars pw) {
         if (turn == 0) {
             List<Planet> planetList = pw.planets();
             int planetsAmount = planetList.size();
 
             distances = new int[planetsAmount][planetsAmount];
 
             growth = new int[planetsAmount];
 
             sortedPlanets = new ArrayList<List<Integer>>(planetsAmount);
 
             avgDistance = 0;
             int avgNum = 0;
             for (int i = 0; i < planetsAmount; ++i) {
                 Planet source = planetList.get(i);
                 ArrayList<Integer> sortedPlanetsRow = new ArrayList<Integer>(planetsAmount - 1);
                 for (int j = 0 ; j < planetsAmount; ++j) {
                     Planet destination = planetList.get(j);
                     double dx = source.X() - destination.X();
                     double dy = source.Y() - destination.Y();
                     distances[i][j] = (int)Math.ceil(Math.sqrt(dx * dx + dy * dy));
 
                     if (i != j) {
                         avgDistance += distances[i][j];
                         ++avgNum;
                         sortedPlanetsRow.add(j);
                     }
                     if (distances[i][j] > maxDistance)
                         maxDistance = distances[i][j];
                 }
                 final int currentPlanet = i;
                 Collections.sort(sortedPlanetsRow, new Comparator<Integer>() {
                     @Override
                     public int compare(Integer planet1, Integer planet2) {
                         return new Integer(distances[currentPlanet][planet1]).
                                 compareTo(distances[currentPlanet][planet2]);
                     }
                 });
                 sortedPlanets.add(sortedPlanetsRow);
                 growth[i] = planetList.get(i).getGrowthRate();
             }
             avgDistance = (int) (((double)avgDistance)/avgNum);
 
         }
         ++turn;
         SquareMatrix transitions;
         try {
             transitions = selectAction(createState(pw));
         } catch (Exception e) {
             Log.error(turn, e);
             return;
         }
         for (int i = 0; i < transitions.size(); ++i) {
             for (int j = 0; j < transitions.size(); ++j) {
                 if (transitions.get(i, j) != 0) {
                     int numShips = transitions.get(i, j);
                     issueOrder(pw, i, j, numShips);
                 }
             }
         }
     }
 
     private static void issueOrder(PlanetWars pw, int i, int j, int numShips) {
         Log.log(turn, "Issuing order: " + i + " " + j + " " + numShips);
         if (pw.getPlanet(i).getOwner() != PlanetWarsState.ME) {
             Log.error("Not my planet!");
             if (SKIP_ON_ERROR)
                 return;
         }
         if (pw.getPlanet(i).getNumShips() < numShips) {
             Log.error("Not enough ships!");
             if (SKIP_ON_ERROR)
                 return;
         }
         if (numShips < 0) {
             Log.error("Negative ships!");
             if (SKIP_ON_ERROR)
                 return;
         }
         pw.IssueOrder(i, j, numShips);
     }
 
     private static SquareMatrix selectAction(PlanetWarsState state) {
         List<List<SquareMatrix>> guessedPlans = guessGoodPlans(state);
         return findBestPlan(state, guessedPlans).get(0);
     }
 
     private static PlanetWarsState createState(PlanetWars pw) {
         List<int[]> planetsInTime = new ArrayList<int[]>();
         List<int[]> ownersInTime = new ArrayList<int[]>();
 
         int planetsAmount = pw.planets().size();
         int[] planets = new int[planetsAmount];
         int[] owners = new int[planetsAmount];
         for (Planet planet : pw.planets()) {
             growth[planet.getPlanetId()] = planet.getGrowthRate();
             if (planet.getOwner() == PlanetWarsState.ME) {
                 planets[planet.getPlanetId()] = planet.getNumShips();
                 owners[planet.getPlanetId()] = 1;
             } else {
                 planets[planet.getPlanetId()] = -planet.getNumShips();
                 if (planet.getOwner() == PlanetWarsState.ENEMY) {
                     owners[planet.getPlanetId()] = -1;
                 } else {
                     owners[planet.getPlanetId()] = 0;
                 }
             }
         }
 
         List<int[]> arrivalsInTime = new ArrayList<int[]>();
         for (Fleet f : pw.fleets()) {
             int turns = f.getTurnsRemaining();
             ListIterator<int[]> iter = arrivalsInTime.listIterator();
             int[] arrivals = null;
             for (int i = 0; i < turns; ++i) {
                 if (iter.hasNext())
                     arrivals = iter.next();
                 else {
                     iter.add(arrivals = new int[planetsAmount]);
                 }
             }
             if (arrivals == null)
                 throw new RuntimeException();
             if (f.getOwner() == PlanetWarsState.ME)
                 arrivals[f.getDestinationPlanet()] += f.getNumShips();
             else
                 arrivals[f.getDestinationPlanet()] -= f.getNumShips();
         }
         planetsInTime.add(planets);
         ownersInTime.add(owners);
 
 
         return new PlanetWarsState(planetsInTime, ownersInTime, arrivalsInTime);
     }
 
     private static List<SquareMatrix> findBestPlan(PlanetWarsState state, List<List<SquareMatrix>> plans) {
         List<SquareMatrix> bestPlan = plans.get(0);
         if (Log.isEnabled()) {
             Log.log("Going to score plan 0");
             Log.log(planToString(state, bestPlan));
         }
 
         int bestScore = score(state, bestPlan);
         if (Log.isEnabled())
             Log.log("Plan scored: score " + bestScore + " for plan 0");
 
         for (int i = 1; i < plans.size(); ++i) {
             if (Log.isEnabled()) {
                 Log.log("Going to score plan " + i);
                 Log.log(planToString(state, plans.get(i)));
             }
             int score = score(state, plans.get(i));
             if (shouldStop())
                 return bestPlan;
             if (score > bestScore) {
                 if (Log.isEnabled()) {
                     StringBuilder sb = new StringBuilder("Plan scored: new best score ").append(score).append(" instead of ").
                             append(bestScore).append(" for plan ").append(i);
                     Log.log(sb.toString());
                 }
                 bestScore = score;
                 bestPlan = plans.get(i);
             } else {
                 if (Log.isEnabled()) {
                     StringBuilder sb = new StringBuilder("Plan scored: score ").append(score)
                             .append(" for plan ").append(i);
                     Log.log(sb.toString());
                 }
             }
         }
         return bestPlan;
     }
 
     private static boolean shouldStop() {
         //noinspection SimplifiableIfStatement
         if ("true".equals(System.getProperty("debug"))) {
             return false;
         }
         boolean shouldExit = System.currentTimeMillis() - start > TIMESTOP;
         if (shouldExit) {
             Log.log(turn, "Exiting by timeout");
         }
         return shouldExit;
     }
 
     private static int score(PlanetWarsState initialState, List<SquareMatrix> plan) {
         List<List<SquareMatrix>> antiPlans = new LinkedList<List<SquareMatrix>>();
         antiPlans.add(doNothingPlan(initialState));
         antiPlans.addAll(AttackTargetPlanetAntiPlans.attackTargetPlanetAntiPlans(initialState, plan));
         antiPlans.addAll(AttackSourcePlanetAntiPlans.attackSourcePlanetAntiPlans(initialState, plan));
 //        antiPlans.add(attackWeakestPlanetAntiPlan(initialState));
 
         int worseScore = Integer.MAX_VALUE;
         for (List<SquareMatrix> antiPlan : antiPlans) {
             PlanetWarsState state = initialState.copy();
             if (Log.isEnabled()) {
                 Log.log("Processing anti plan");
                 Log.log(planToString(state, antiPlan));
             }
 
 
             state.setMyPlan(plan.iterator());
             state.setEnemyPlan(antiPlan.iterator());
             Result result;
             //noinspection StatementWithEmptyBody
             for (int i = 0; i < maxDistance; ++i) {
                 result = state.evaluateTurn();
                 if (result == Result.FAILED)
                     return Integer.MIN_VALUE;
             }
 
 //        int score = scoreNumShipsCoulomb(state);
             int score = scoreNumShips(state);
 //            int score = scoreNumPlanets(state);
             if (score < worseScore) {
                 if (Log.isEnabled()) {
                     StringBuilder sb = new StringBuilder("Processed anti-plan: new worseScore ").append(score).
                             append(" instead of ").append(worseScore);
                     Log.log(sb.toString());
                 }
                 worseScore = score;
             } else if (Log.isEnabled()) {
                 StringBuilder sb = new StringBuilder("Anti-plan processed : score ").append(score);
                 Log.log(sb.toString());
             }
         }
         return worseScore;
     }
 
     private static int scoreNumShips(PlanetWarsState state) {
         int[] lastPlanets = state.getPlanetsInTime().get(state.getPlanetsInTime().size() - 1);
         int[] lastOwners = state.getOwnersInTime().get(state.getOwnersInTime().size() - 1);
         int sumShips = 0;
         for (int i = 0; i < lastPlanets.length; ++i) {
             if (lastOwners[i] != 0) {
                 sumShips += lastPlanets[i];
             }
         }
         return sumShips;
     }
 
     private static int scoreNumShipsCoulomb(PlanetWarsState state) {
         int[] planets = state.getPlanetsInTime().get(state.getPlanetsInTime().size() - 1);
         int[] owners = state.getOwnersInTime().get(state.getOwnersInTime().size() - 1);
         int score = 0;
         for (int i = 0; i < planets.length; ++i) {
             for (int j = 0; j <= i; ++j) {
                 if (owners[i] == 1 && owners[j] == 1) {
                     if (i != j) {
                         int d = distances[i][j];
                         score += (int) ((double) planets[i]) * planets[j] / (d*d);
                     } else {
 //                        score += planets[i] * planets[j];
                     }
                 }
             }
         }
         return score;
 //        return score;
     }
 
     private static int scoreNumPlanets(PlanetWarsState state) {
         int[] owners = state.getOwnersInTime().get(state.getOwnersInTime().size() - 1);
         int numPlanets = 0;
         for (int owner : owners) {
             numPlanets += owner;
         }
         return numPlanets;
     }
 
     private static List<SquareMatrix> attackWeakestPlanetAntiPlan(PlanetWarsState state) {
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
 
         SquareMatrix antiTurn = new SquareMatrix(planets.length);
         List<SquareMatrix> antiPlan = new LinkedList<SquareMatrix>();
         antiPlan.add(antiTurn);
 
         int weakest = Integer.MAX_VALUE;
         int weakestId = -1;
         int strongest = Integer.MIN_VALUE;
         int strongestId = -1;
         for (int i = 0; i < planets.length; ++i) {
             if (owners[i] == 1) {
                 if (weakest > planets[i]) {
                     weakest = planets[i];
                     weakestId = i;
                 }
             } else if (owners[i] == -1) {
                 if (strongest < -planets[i]) {
                     strongest = planets[i];
                     strongestId = i;
                 }
             }
         }
 
         int requiredNumShips = planets[weakestId] + growth[weakestId] * distances[strongestId][weakestId] + 1;
         if (strongest > requiredNumShips)
             antiTurn.set(strongestId, weakestId, -requiredNumShips);
 
         return antiPlan;
     }
 
     public static void printPlanet(StringBuilder sb, int[] planets, int[] owners, int i) {
         sb.append(i).append("(").
                 append(planets[i]).append("/").
                 append(owners[i]).append("/").
                 append(growth[i]).append(")");
     }
 
     private static String planToString(PlanetWarsState state, List<SquareMatrix> plan) {
         StringBuilder sb = new StringBuilder();
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
         for (int t = 0; t < plan.size(); ++t) {
             sb.append("\n");
             sb.append("Print plan t: ").append(t).append(" - ");
             SquareMatrix tr = plan.get(t);
             for (int i = 0; i < tr.size(); ++i) {
                 for (int j = 0; j < tr.size(); ++j) {
                     if (tr.get(i, j) != 0) {
                         printPlanet(sb, planets, owners, i);
                         sb.append(", ");
                         printPlanet(sb, planets, owners, j);
                         sb.append(", num ships: ").append(tr.get(i, j));
                         sb.append(", distance: ").append(distances[i][j]);
                     }
                 }
             }
         }
         return sb.toString();
     }
 
     public static int calculateAroundShips(PlanetWarsState state, int sourcePlanetId, int targetPlanetId) {
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
         int sourceOwner = owners[sourcePlanetId];
 
         int aroundShips = 0;
         for (int i = 0; i < planets.length; ++i) {
             if (i == sourcePlanetId || i == targetPlanetId) {
                 continue;
             }
             if (owners[i] * sourceOwner == -1 && distances[i][targetPlanetId] < distances[sourcePlanetId][targetPlanetId]) {
                 aroundShips += owners[i] * planets[i];
             }
         }
 
         return aroundShips;
     }
 
     private static List<List<SquareMatrix>> guessGoodPlans(PlanetWarsState state) {
         List<List<SquareMatrix>> plan = new ArrayList<List<SquareMatrix>>();
         plan.add(doNothingPlan(state));
         plan.addAll(kickOutPlans(state));
         plan.addAll(takeOnePlanetPlans(state, 0));
         for (int i = 95; i >= 0; i-=15) {
             plan.addAll(takeOnePlanetPlans(state, ((float) (100 - i)) / 100));
         }
         for (int i = 95; i >= 0; i-=15) {
             plan.addAll(onePlanetDefenseAnotherPlans(state, ((float) (100 - i)) / 100));
         }
 
         plan.addAll(exchangeShipsPlan(state, 0.9));
         plan.addAll(exchangeShipsPlan(state, 0.6));
         plan.addAll(exchangeShipsPlan(state, 0.3));
         plan.addAll(exchangeShipsPlan(state, 0.1));
         plan.add(attackAllPlanetsPlan(state));
 
         return plan;
     }
 
     private static List<List<SquareMatrix>> exchangeShipsPlan(PlanetWarsState state, double factor) {
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
 
 
         List<List<SquareMatrix>> plans = new LinkedList<List<SquareMatrix>>();
         for (int i = 0; i < planets.length; ++i) {
             if (owners[i] == 1) {
                 for (int j = 0; j < i; ++j) {
                     if (owners[j] == 1) {
                         SquareMatrix transitions = new SquareMatrix(planets.length);
                         transitions.set(i, j, (int)factor*planets[i]);
                         List<SquareMatrix> transitionsInTime = new LinkedList<SquareMatrix>();
                         transitionsInTime.add(transitions);
                         plans.add(transitionsInTime);
                     }
                 }
             }
         }
 
 
 
         return plans;
     }
 
     private static List<SquareMatrix> attackAllPlanetsPlan(PlanetWarsState state) {
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
 
         SquareMatrix transitions = new SquareMatrix(planets.length);
 
         int notMyPlanetsNum = 0;
         for (int i = 0; i < planets.length; ++i) {
             if (owners[i] < 1) {
                 ++notMyPlanetsNum;
             }
         }
 
         for (int i = 0; i < planets.length; ++i) {
             if (owners[i] == 1) {
                 int numShips = planets[i];
                 if (numShips < notMyPlanetsNum) {
                     continue;
                 }
                 for (int j = 0; j < planets.length; ++j) {
                     if (owners[i] < 1) {
                         transitions.set(i, j, numShips/notMyPlanetsNum);
                     }
                 }
             }
         }
         List<SquareMatrix> transitionsInTime = new LinkedList<SquareMatrix>();
         transitionsInTime.add(transitions);
 
         return transitionsInTime;
     }
 
     private static List<SquareMatrix> doNothingPlan(PlanetWarsState state) {
         int[] planets = state.getPlanetsInTime().get(0);
         SquareMatrix transitions = new SquareMatrix(planets.length);
         List<SquareMatrix> transitionsInTime = new LinkedList<SquareMatrix>();
         transitionsInTime.add(transitions);
 
         return transitionsInTime;
     }
 
     public static int requiredNumShips(PlanetWarsState state, int source, int target, boolean countGrowth) {
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
 
         int requiredNumShips = -planets[target] + 1;
         int distance = distances[source][target];
         for (int t = 0; t < distance; ++t) {
             if (t < state.getArrivalsInTime().size())
                 requiredNumShips += -state.getArrivalsInTime().get(t)[target];
             if (countGrowth)
                 requiredNumShips += -owners[target]*growth[target];
         }
         for (int i = 0; i < planets.length; ++i) {
             if (i != target && owners[i] < 0 && distances[i][target] < distances[source][target]) {
                 requiredNumShips += -planets[i];
                 requiredNumShips += growth[i] * (distances[source][target] - distances[i][target]);
             }
         }
         return requiredNumShips;
     }
 
     public static int requiredNumShipsForAttackAntiPlan(PlanetWarsState state, int source, int target) {
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
 
         int requiredNumShips;
         if (owners[target] == 1)
             requiredNumShips = planets[target] + 1;
         else
             requiredNumShips = -planets[target] + 1;
         int distance = distances[source][target];
         for (int t = 0; t < distance; ++t) {
             if (t < state.getArrivalsInTime().size())
                 requiredNumShips += state.getArrivalsInTime().get(t)[target];
             requiredNumShips += owners[target]*growth[target];
         }
         return requiredNumShips;
     }
 
     private static List<List<SquareMatrix>> kickOutPlans(PlanetWarsState state) {
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
         List<int[]> arrivalsInTime = state.getArrivalsInTime();
 
         List<List<SquareMatrix>> plans = new ArrayList<List<SquareMatrix>>();
 
         for (int t = 0; t < arrivalsInTime.size(); ++t) {
             int[] arrivals = arrivalsInTime.get(t);
             for (int i = 0; i < arrivals.length; ++i) {
                 if (arrivals[i] < 0 && owners[i] == 0 && arrivals[i] < planets[i]) {
                     List<Integer> sortedPlanetsRow = sortedPlanets.get(i);
                     for (int j : sortedPlanetsRow) {
                         if (owners[j] > 0) {
                             int requiredNumShips = requiredNumShips(state, j, i, false);
                             //TODO: Explain why +2?
                             int timeToStart = t - distances[i][j] + 2;
                             int numShipsOnMyPlanet = planets[j] +  timeToStart * growth[j];
                             if (requiredNumShips > 0 && numShipsOnMyPlanet >= requiredNumShips) {
                                 List<SquareMatrix> plan = new ArrayList<SquareMatrix>();
                                 plans.add(plan);
                                 SquareMatrix tr;
                                 plan.add(tr = new SquareMatrix(planets.length));
                                 for (int k = 0; k < timeToStart; ++k) {
                                     plan.add(tr = new SquareMatrix(planets.length));
                                 }
                                 //noinspection ConstantConditions
                                 tr.set(j, i, requiredNumShips);
                             }
                         }
                     }
                 }
             }
         }
 
         return plans;
     }
 
     private static List<List<SquareMatrix>> takeOnePlanetPlans(PlanetWarsState state, float defenseFactor) {
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
 
         List<List<SquareMatrix>> plans = new ArrayList<List<SquareMatrix>>();
 
         for (int i = 0; i < planets.length; ++i) {
             if (owners[i] < 1) {
                 List<Integer> sortedPlanetsRow = sortedPlanets.get(i);
                 for (int j : sortedPlanetsRow) {
                     if (owners[j] == 1) {
                         int requiredNumShips = requiredNumShips(state, j, i, true);
                         if (planets[j] * (1.0 - defenseFactor) > requiredNumShips && requiredNumShips > 0) {
                             SquareMatrix transitions = new SquareMatrix(planets.length);
                             transitions.set(j, i, requiredNumShips);
                             List<SquareMatrix> transitionsInTime = new LinkedList<SquareMatrix>();
                             transitionsInTime.add(transitions);
                             plans.add(transitionsInTime);
                         }
                     }
                 }
             }
         }
 
         return plans;
     }
 
     private static List<List<SquareMatrix>> onePlanetDefenseAnotherPlans(PlanetWarsState state, double factor) {
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
 
         List<List<SquareMatrix>> plans = new ArrayList<List<SquareMatrix>>();
 
         for (int t = 0; t < state.getArrivalsInTime().size(); ++t) {
             for (int i = 0; i < planets.length; ++i) {
                 if (owners[i] != -1) {
                     if (state.getArrivalsInTime().get(t)[i] < 0) {
                         for (int j = 0; j < planets.length; ++j) {
                             if (i != j && owners[j] == 1) {
                                 SquareMatrix tr = new SquareMatrix(planets.length);
                                 tr.set(j, i, (int) (planets[j] * factor));
                                 List<SquareMatrix> transitionsInTime = new LinkedList<SquareMatrix>();
                                 transitionsInTime.add(tr);
                                 plans.add(transitionsInTime);
                             }
                         }
                     }
                 }
             }
         }
 
         return plans;
     }
 
     private static List<List<SquareMatrix>> takeMultiplePlanetPlans(PlanetWarsState state) {
         int[] planets = state.getPlanetsInTime().get(0);
         int[] owners = state.getOwnersInTime().get(0);
 
         List<List<SquareMatrix>> plans = new ArrayList<List<SquareMatrix>>();
 
         for (int i = 0; i < planets.length; ++i) {
             if (owners[i] < 1) {
                 for (int j = 0; j < planets.length; ++j) {
                     if (owners[j] == 1) {
                         int requiredNumShips = -planets[i] + 1;
                         int distance;
                         if (j < i)
                             distance = distances[i][j];
                         else
                             distance = distances[j][i];
                         for (int t = 0; t < distance && t < state.getArrivalsInTime().size(); ++t) {
                             requiredNumShips += -state.getArrivalsInTime().get(t)[i];
                             if (owners[i] == -1) {
                                 requiredNumShips += growth[i];
                             }
                         }
                         if (planets[j] > requiredNumShips && requiredNumShips > 0) {
                             SquareMatrix transitions = new SquareMatrix(planets.length);
                             List<SquareMatrix> transitionsInTime = new LinkedList<SquareMatrix>();
                             transitions.set(j, i, requiredNumShips);
                             transitionsInTime.add(transitions);
                             plans.add(transitionsInTime);
                         }
                     }
                 }
             }
         }
 
         return plans;
     }
 
     public static void main(String[] args) {
         String line = "";
         String message = "";
         int c;
         try {
             while ((c = System.in.read()) >= 0) {
                 switch (c) {
                     case '\n':
                         if (line.equals("go")) {
                             start = System.currentTimeMillis();
                             if (turn % 10 == 0)
                                 System.gc();
                             PlanetWars pw = new PlanetWars(message);
                             DoTurn(pw);
                             pw.FinishTurn();
                             message = "";
                         } else {
                             message += line + "\n";
                         }
                         line = "";
                         break;
                     default:
                         line += (char) c;
                         break;
                 }
             }
         } catch (Exception e) {
             Log.error(turn, e);
         } catch (Throwable t) {
             Log.error(turn, t);
         }
     }
 }
 
