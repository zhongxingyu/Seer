 package euler;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import euler.numberic.Number;
 
 public abstract class Problem<T> {
     private static final Map<Integer, java.lang.Number> knownSolutions = new HashMap<Integer, java.lang.Number>();
     static {
         knownSolutions.put(1, Number.valueOf(233168));
         knownSolutions.put(2, Number.valueOf(4613732));
         knownSolutions.put(3, Number.valueOf(6857));
         knownSolutions.put(4, Number.valueOf(906609));
         knownSolutions.put(5, Number.valueOf(232792560));
         knownSolutions.put(6, Number.valueOf(25164150));
         knownSolutions.put(7, Number.valueOf(104743));
         knownSolutions.put(8, Number.valueOf(40824));
         knownSolutions.put(9, Number.valueOf(31875000));
         knownSolutions.put(10, Number.valueOf(142913828922l));
         knownSolutions.put(11, Number.valueOf(70600674));
         knownSolutions.put(12, Number.valueOf(76576500));
         knownSolutions.put(13, Number.valueOf(5537376230l));
         knownSolutions.put(14, Number.valueOf(837799));
         knownSolutions.put(15, Number.valueOf(137846528820l));
         knownSolutions.put(16, Number.valueOf(1366));
         knownSolutions.put(17, Number.valueOf(21124));
         knownSolutions.put(18, Number.valueOf(1074));
         knownSolutions.put(19, Number.valueOf(171));
         knownSolutions.put(20, Number.valueOf(648));
         knownSolutions.put(21, Number.valueOf(31626));
         knownSolutions.put(22, Number.valueOf(871198282));
         knownSolutions.put(23, Number.valueOf(4179871));
         knownSolutions.put(24, Number.valueOf(2783915460l));
         knownSolutions.put(25, Number.valueOf(4782));
         knownSolutions.put(26, Number.valueOf(983));
         knownSolutions.put(27, -59231);
         knownSolutions.put(28, Number.valueOf(669171001));
         knownSolutions.put(29, Number.valueOf(9183));
         knownSolutions.put(30, Number.valueOf(443839));
         knownSolutions.put(31, Number.valueOf(73682));
         knownSolutions.put(32, Number.valueOf(45228));
         knownSolutions.put(33, Number.valueOf(100));
         knownSolutions.put(34, Number.valueOf(40730));
         knownSolutions.put(35, Number.valueOf(55));
         knownSolutions.put(36, Number.valueOf(872187));
         knownSolutions.put(37, Number.valueOf(748317));
         knownSolutions.put(38, Number.valueOf(932718654));
         knownSolutions.put(39, Number.valueOf(840));
         knownSolutions.put(40, Number.valueOf(210));
         knownSolutions.put(41, Number.valueOf(7652413));
         knownSolutions.put(42, Number.valueOf(162));
         knownSolutions.put(43, Number.valueOf(16695334890l));
         knownSolutions.put(44, Number.valueOf(5482660));
         knownSolutions.put(45, Number.valueOf(1533776805));
         knownSolutions.put(46, Number.valueOf(5777));
         knownSolutions.put(47, Number.valueOf(134043));
         knownSolutions.put(48, Number.valueOf(9110846700l));
         knownSolutions.put(49, Number.valueOf(296962999629l));
         knownSolutions.put(50, Number.valueOf(997651));
         knownSolutions.put(51, Number.valueOf(121313));
         knownSolutions.put(52, Number.valueOf(142857));
         knownSolutions.put(53, Number.valueOf(4075));
         knownSolutions.put(54, Number.valueOf(376));
         knownSolutions.put(55, Number.valueOf(249));
         knownSolutions.put(56, Number.valueOf(972));
         knownSolutions.put(57, Number.valueOf(153));
         knownSolutions.put(58, Number.valueOf(26241));
         knownSolutions.put(59, Number.valueOf(107359));
         knownSolutions.put(60, Number.valueOf(26033));
         knownSolutions.put(61, Number.valueOf(28684));
         knownSolutions.put(62, Number.valueOf(127035954683l));
         knownSolutions.put(63, Number.valueOf(49));
         knownSolutions.put(64, Number.valueOf(1322));
         knownSolutions.put(65, Number.valueOf(272));
         knownSolutions.put(66, Number.valueOf(661));
         knownSolutions.put(67, Number.valueOf(7273));
         knownSolutions.put(68, Number.valueOf(6531031914842725l));
         knownSolutions.put(69, Number.valueOf(510510));
         knownSolutions.put(70, Number.valueOf(8319823));
         knownSolutions.put(71, Number.valueOf(428570));
         knownSolutions.put(72, Number.valueOf(303963552391l));
         knownSolutions.put(73, Number.valueOf(7295372));
         knownSolutions.put(74, Number.valueOf(402));
         knownSolutions.put(75, Number.valueOf(161667));
         knownSolutions.put(76, Number.valueOf(190569291));
         knownSolutions.put(77, Number.valueOf(71));
         knownSolutions.put(78, Number.valueOf(55374));
         knownSolutions.put(79, Number.valueOf(73162890));
         knownSolutions.put(80, Number.valueOf(40886));
         knownSolutions.put(81, Number.valueOf(427337));
         knownSolutions.put(82, Number.valueOf(260324));
         knownSolutions.put(83, Number.valueOf(425185));
         knownSolutions.put(84, Number.valueOf(101524));
         knownSolutions.put(85, Number.valueOf(2772));
         knownSolutions.put(86, Number.valueOf(1818));
         knownSolutions.put(87, Number.valueOf(1097343));
         knownSolutions.put(88, Number.valueOf(7587457));
         knownSolutions.put(89, Number.valueOf(743));
         knownSolutions.put(90, Number.valueOf(1217));
         knownSolutions.put(91, Number.valueOf(14234));
         knownSolutions.put(92, Number.valueOf(8581146));
        knownSolutions.put(92, Number.valueOf(1258));
         knownSolutions.put(94, Number.valueOf(518408346));
         knownSolutions.put(95, Number.valueOf(14316));
         knownSolutions.put(96, Number.valueOf(24702));
         knownSolutions.put(97, Number.valueOf(8739992577l));
         knownSolutions.put(99, Number.valueOf(709));
         knownSolutions.put(100, Number.valueOf(756872327473l));
         knownSolutions.put(102, Number.valueOf(228));
         knownSolutions.put(104, Number.valueOf(329468));
         knownSolutions.put(108, Number.valueOf(180180));
         knownSolutions.put(110, Number.valueOf(9350130049860600l));
         knownSolutions.put(111, Number.valueOf(612407567715l));
         knownSolutions.put(113, Number.valueOf(51161058134250l));
         knownSolutions.put(114, Number.valueOf(16475640049l));
         knownSolutions.put(115, Number.valueOf(168));
         knownSolutions.put(116, Number.valueOf(20492570929l));
         knownSolutions.put(117, Number.valueOf(100808458960497l));
         knownSolutions.put(119, Number.valueOf(248155780267521l));
         knownSolutions.put(120, Number.valueOf(333082500));
         knownSolutions.put(123, Number.valueOf(21035));
         knownSolutions.put(124, Number.valueOf(21417));
         knownSolutions.put(125, Number.valueOf(2906969179l));
         knownSolutions.put(142, Number.valueOf(1006193));
         knownSolutions.put(160, Number.valueOf(16576));
         knownSolutions.put(162, Number.valueOf(0x3D58725572C62302L));
         knownSolutions.put(164, Number.valueOf(378158756814587l));
         knownSolutions.put(165, Number.valueOf(2868868));
         knownSolutions.put(204, Number.valueOf(2944730));
         knownSolutions.put(205, Double.valueOf(0.5731441));
         knownSolutions.put(206, Number.valueOf(1389019170));
         knownSolutions.put(357, Number.valueOf(1739023853137l));
     }
 
     private static final long execute(int nr) {
         Problem<?> problem = null;
         final int level = (nr - 1) / 50 + 1;
         try {
             final Class<?> clazz = Class.forName(String.format("euler.level%d.Problem%03d", level, nr));
             problem = (Problem<?>) clazz.newInstance();
         } catch (final ClassNotFoundException e) {
             throw new IllegalArgumentException(String.format("The given problem number (%d) could not be found", nr));
         } catch (final InstantiationException e) {
             throw new IllegalArgumentException(String.format("The given problem number (%d) could not be instantiated", nr));
         } catch (final IllegalAccessException e) {
             throw new IllegalArgumentException(String.format("The given problem number (%d) could not be accessed", nr));
         }
 
         return execute(problem, knownSolutions.get(nr));
     }
 
     private static final <T> long execute(Problem<T> problem, java.lang.Number knownSolution) {
         final long start = System.nanoTime();
         final T result = problem.solve();
         final long time = System.nanoTime() - start;
 
         if (result == null) {
             System.out.printf("          Result not found for %-20s Calculated in %5.2f seconds%n",
                               problem.getClass().getSimpleName(),
                               time / 1e9);
             return -1;
         } else {
             final String checked = knownSolution == null ? "Unchecked" : knownSolution.equals(result) ? "Correct" : "Incorrect";
             System.out.printf("%9s result for %s: %-18s Calculated in %5.3f seconds%n",
                               checked,
                               problem.getClass().getSimpleName(),
                               result.toString(),
                               time / 1e9);
             return time;
         }
     }
 
     public static void main(String[] args) {
         if (args.length == 0) {
             final int MAX = 400;
 
             System.out.println("Executing all problems...");
             int found = 0, executed = 0, missing = 0;
             long totalTime = 0;
             for (int i = 1; i < MAX; i++) {
                 try {
                     final long time = Problem.execute(i);
                     if (time >= 0) {
                         found++;
                         totalTime += time;
                     }
                     executed++;
                 } catch (final IllegalArgumentException ex) {
                     if (knownSolutions.containsKey(i)) {
                         System.out.println(ex.getMessage());
                         missing++;
                     }
                 }
             }
             System.out.printf("Problems solved: %d out of %d (+%d missing)\tTotal duration: %5.2f seconds%n",
                               found,
                               executed,
                               missing,
                               totalTime / 1e9);
         } else {
             int nr = 1;
             try {
                 nr = Integer.parseInt(args[0]);
             } catch (final NumberFormatException ex) {
                 System.out.println("Please give the number of the problem that you want to solve!");
                 return;
             }
 
             Problem.execute(nr);
         }
     }
 
     public abstract T solve();
 }
