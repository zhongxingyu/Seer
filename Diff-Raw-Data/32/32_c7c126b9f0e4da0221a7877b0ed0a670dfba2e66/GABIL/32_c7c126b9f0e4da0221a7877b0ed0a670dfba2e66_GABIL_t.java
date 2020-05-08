 
 package genetico;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Random;
 
 /**
  *
  * @author Lorenzo Fundaró <lorenzofundaro [at] yahoo.com>
  */
 public class GABIL {
     ArrayList<int[]> trainingData;
     int p;     // tamaño de población.
     double r;   // fracción de la población que será reemplazada.
     double m;   // Tasa de mutación.
     double fitness_threshold;
     
     public GABIL(ArrayList<int[]> training, int p, double r, double m,
             double fthld) {
         trainingData = training;
         this.p = p;
         this.r = r;
         this.m = m;
         fitness_threshold = fthld;
     }
     
     private ArrayList<int[]> initPop() {
         // Generación de una hipótesis aleatoria
         ArrayList<int[]> pool = new ArrayList<int[]>(p);
         int[] hyp;
         for(int i = 0; i < p; i++) {
             hyp = new int[Parser.REP_SIZE*2]; // Para generar hyp con dos reglas.
             init(hyp);
             generators(hyp, 0, Parser.REP_SIZE);
             generators(hyp, Parser.REP_SIZE, Parser.REP_SIZE*2);
             pool.add(hyp);
         }
         return pool;
     }
     
     private void generators(int[] hyp, int l, int h) {
         int index = l;
         Random gen = new Random();
         // Edad
         index = genField(hyp, gen, index, index + Parser.AGE_SIZE, "AGE");
         // Educación de la esposa
         index = genField(hyp, gen, index, index + Parser.CAT_SIZE, "CAT");
         // Educación del esposo
         index = genField(hyp, gen, index, index + Parser.CAT_SIZE, "CAT");
         // Número de hijos
         index = genField(hyp, gen, index, index + Parser.NCHILD_SIZE,
                 "CHILD");
         // Religión
         index = genField(hyp, gen, index, index + Parser.REL_SIZE, "REL");
         // Situación trabajo esposa
         index = genField(hyp, gen, index, index + Parser.WORK_SIZE, "WORK");
         // Situación del esposo
         index = genField(hyp, gen, index, index + Parser.CAT_SIZE, "CAT");
         // Estándar de vida
         index = genField(hyp, gen, index, index + Parser.CAT_SIZE, "CAT");
         // Exposición al ambiente
         index = genField(hyp, gen, index, index + Parser.MEX_SIZE, "MEX");
         // Método anticonceptivo
         genMethodField(hyp, gen, index, index + Parser.MTH_SIZE);
     }
     
     private void genMethodField(int[] hyp, Random gen, int l, int h) {
         double g = gen.nextDouble();
         int method = 1 + (int) (g*(3));
         for(int i = l; i < h; i++)
             hyp[i] = method;
         return;
     }
     
     
     private int genField(int[] hyp, Random gen, int l, int h, String type) {
         double g = gen.nextDouble();
         while (!validGen(hyp, l, h)) {
             if (g <= 0.5) {
                 // Random bit-a-bit
                 for(int i = l; i < h; i++) {
                     g = gen.nextDouble();
                     if (g <= 0.5)   // Bit prendido
                         hyp[i] = 1;
                     else           // Bit apagado
                         hyp[i] = 0;
                 }
             }
             else {
                 // Random global
                 g = gen.nextDouble();
                 int w;
                 if (type.compareTo("AGE") == 0) {
                     w = Parser.MIN_AGE + (int) (g*(Parser.MAX_AGE -
                             Parser.MIN_AGE + 1));
                     Parser.calcAge(hyp, w, l);
                 } else if (type.compareTo("CAT") == 0) {
                     w = 1 + (int) (g*(Parser.CAT_SIZE));
                     Parser.calCat(hyp, w, l);
                 } else if (type.compareTo("CHILD") == 0) {
                     w = Parser.MIN_CHILD + (int) (g*(Parser.MAX_CHILD -
                             Parser.MIN_CHILD + 1));
                     Parser.numChildren(hyp, w, l);
                 } else if (type.compareTo("REL") == 0) {
                     w = (int) (g*(2));
                     Parser.calRel(hyp, w, l);
                 } else if (type.compareTo("WORK") == 0) {
                     w = (int) (g*(2));
                     Parser.calWorking(hyp, w, l);
                 } else if (type.compareTo("MEX") == 0) {
                     w = (int) (g*(2));
                     Parser.calMediaExp(hyp, w, l);
                 }
             }
             g = gen.nextDouble();
         }
         return h;
     }
     
     private boolean validGen(int[] hyp, int l, int h) {
         int valid = 0;
         for(int i = l; i < h; i++) {
             valid = valid | hyp[i];
         }
         return valid == 1;
     }
 
     public boolean validHyp(int[] hyp){
 
         for (int i=0; i<hyp.length/Parser.REP_SIZE; i++){
             int offset = Parser.REP_SIZE*i;
             if (!validGen(hyp,0+offset,7+offset)) return false;
             if (!validGen(hyp,7+offset,11+offset)) return false;
             if (!validGen(hyp,11+offset,15+offset)) return false;
             if (!validGen(hyp,15+offset,21+offset)) return false;
             if (!validGen(hyp,21+offset,23+offset)) return false;
             if (!validGen(hyp,23+offset,25+offset)) return false;
             if (!validGen(hyp,25+offset,29+offset)) return false;
             if (!validGen(hyp,29+offset,33+offset)) return false;
             if (!validGen(hyp,33+offset,35+offset)) return false;
         }
 
         return true;
     }
     
     private void init(int[] ex) {
         for(int i = 0; i < ex.length; i++)
             ex[i] = 0;
     }
     
     private double[] fitness(ArrayList<int[]> pool) {
         Iterator<int[]> it = pool.iterator();
         double[] ftn = new double[pool.size()];
         int cnt = 0;
         double overall = 0.0;
         while (it.hasNext()) {
             int[] hyp = it.next();
             int nrules = hyp.length / Parser.REP_SIZE;
             int i = 0;
             int acc = 0;
             int index = 0;
             // Ṕrobar cada regla contra el training set.
             while (i < nrules) {
                 acc += testAgainstTS(hyp, index);
                 i++;
                 index += Parser.REP_SIZE;
             }
             // Calcular fitness overall de la hipótesis
            overall = ((((double) acc) / ((double) trainingData.size())) 
                    / ((double) nrules));
             // Asignar a la i-ésima hipótesis su valor fitness
             ftn[cnt] = overall;
             cnt++;
         }
         return ftn;
     }
     
     private int testAgainstTS(int[] hyp, int l) {
         Iterator<int[]> it = trainingData.iterator();
         int classified = 0;
         while (it.hasNext()) {
             int[] example = it.next();
             int field = 0;
             int index = l;
             boolean testOK = true;
             while (testOK && field < Parser.FIELDS) {
                 testOK &= matchRulePortion(hyp, example, index,
                         index + Parser.seq[field]);
                 index += Parser.seq[field];
                 field++;
             }
             if (testOK) {classified++;}
         }
         return classified;
     }
     
     private boolean matchRulePortion(int[] hyp, int[] example, int l, int h) {
         int valid = 0;
         // Check si se hace match con Method
         if (l != 0 && (l % 35 == 0)) {
            return (example[example.length - 1] == hyp[l]);
         } else {
             for(int i = l; i < h; i++) {
                 valid |= (hyp[i] & example[i % 36]);
             }
         }
         return valid == 1;
     }
     
     public void printHyp(int[] h) {
         for(int i = 0; i < h.length; i++) {
             System.out.print(h[i]);
         }
         System.out.println("");
     }
     
     private Pair max(double[] ftn) {
         double h = 0.0;
         double dup = h;
         int index = 0;
         for(int i = 0; i < ftn.length; i++) {
             h = Math.max(h, ftn[i]);
             if (h != dup) {index = i;}
             dup = h;
         }
         Pair res = new Pair(index, h);
         return res;
     }
     
     private ArrayList<int[]> rouletteSelection(int prop,
             ArrayList<int[]> population, ArrayList<Pair> Pr) {
         Random gen = new Random();
         ArrayList<int[]> Ps = new ArrayList<int[]>(prop);
         double acc = 0.0;
         Pair tmp;
         Pair aux;
         ArrayList<Pair> AccN = new ArrayList<Pair>(Pr.size());
         Iterator<Pair> it = Pr.iterator();
         double acumulador = 0.0;
         while (it.hasNext()) {
             tmp = (it.next()).clone();
             aux = tmp.clone();
             acumulador += tmp.getRight(); 
             tmp.setRight(tmp.getRight() + acc);
             AccN.add(tmp);
             acc += aux.getRight();
         }
        System.out.println("Accumulated");
        System.out.println(AccN);
 //        System.out.println("Acumulador = "+acumulador);
 //        System.out.println("Size' = "+AccN.size());
         double g;
         for(int i = 0; i < prop; i++) {
             g = gen.nextDouble()*(acumulador); // Rango [0,1]
             it = AccN.iterator();
             while (it.hasNext()) {
                 tmp = it.next();
 //                System.out.println("G = "+g);
 //                System.out.println("Tmp.right = "+tmp.getRight());
                 if (tmp.getRight() >= g) {
                     Ps.add(population.get(tmp.getLeft()));
                     break;
                 }
 //                System.out.println("Size PS = "+Ps.size());
 //                System.out.println("Prop = "+prop);
             }
         }
         System.out.println("prop = "+prop);
         System.out.println("Ps.length = "+Ps.size());
         return Ps;
     }
     
     private ArrayList<Pair> computeProbs(double[] ftn) {
         // Suma de todos los fitness
         double sum = 0.0;
         for(int i = 0; i < ftn.length; i++)
             sum += ftn[i];
         // Probabilidad de cada hipótesis
         double[] pr = new double[ftn.length];
         for(int i = 0; i < pr.length; i++)
             pr[i] = ftn[i] / sum;
         ArrayList<Pair> Pr = new ArrayList<Pair>(ftn.length);
         Pair pip;
         for(int i = 0; i < ftn.length; i++) {
             pip = new Pair(i, ftn[i]);
             Pr.add(pip);
         }
         Collections.sort(Pr, new PairComparator());
         return Pr;
     }
 
     public static int random(int min, int max) {
         max = max + 1 ;
         return min + (int)(Math.random() * (max - min));
     }
 
     public int[] getds(int x1, int x2, int rule_size){
         
         int[] ds = new int[2];
 
         if (x1<=rule_size)
             ds[0]=x1;
         else
             ds[0]=x1%rule_size;
 
         if (x2<=rule_size)
             ds[1]=x2;
         else
             ds[1]=x2%rule_size;
 
         return ds;
         
     }
 
     private ArrayList<int []> crossover(int n_cross, 
             ArrayList<int[]> population, double[] ftn, ArrayList<int[]> Ps,
             ArrayList<Pair> Pr){
 
         for (int i_cross=0; i_cross<n_cross; i_cross++){
 
             ArrayList<int []> padres = rouletteSelection(2, population, Pr);
 
             // Seleccionar probabilisticamente un par de padres (rueda de ruleta)
             int[] p1 = padres.get(0);
             int[] p2 = padres.get(1);
 
             // Se calcula el par de corte para el primer padre
             System.out.println("p1.length ="+p1.length);
             int x1 = random(1,p1.length);
             int x2 = random(x1,p1.length);
 
             int[] ds = getds(x1,x2,Parser.REP_SIZE);
 
             int[] ds_b = {0,0};
             int x1_b = 0;
             int x2_b = 0;
             // Se calcula el par de corte para el segundo padre
             // y se valida hasta que sea correcto
             // Primer d
             while (ds_b[0]!=ds[0]) {
                 x1_b = random(1,p2.length);
                 ds_b = getds(x1_b,x2,Parser.REP_SIZE);
             }
                     
             while (ds_b[1]!=ds[1] ){
                 x2_b = random(x1_b,p2.length);
                 ds_b = getds(x1,x2_b,Parser.REP_SIZE);
             }
             
 //            while (ds_b[0]!=ds[0] || ds_b[1]!=ds[1] ){
 //                x1_b = random(1,p2.length);
 //                x2_b = random(x1_b,p2.length);
 //                ds_b = getds(x1_b,x2_b,Parser.REP_SIZE);
 //            }
 
             // Se inicializan los nuevos hijos
             System.out.println((x1+1)+(x2_b-x1_b+1)+p1.length);
             System.out.println("x2= "+x2);
             System.out.println("x1 ="+x1);
             int [] hijo1 = new int[(x1+1)+(x2_b-x1_b+1)+p1.length-x2];
             int [] hijo2 = new int[(x1_b+1)+(x2-x1+1)+p2.length-x2_b];
 
             int conth1 = 0;
             int conth2 = 0;
             // Se llenan los hijos
 
             // Hijo1
             for (int i = 0; i<x1 ; i++){
                 hijo1[conth1] = p1[i];
                 conth1++;
             }
             for (int i = x1_b; i<x2_b ; i++){
                 hijo1[conth1] = p2[i];
                 conth1++;
             }
             for (int i = x2; i<p1.length ; i++){
                 hijo1[conth1] = p1[i];
                 conth1++;
             }
 
             // Hijo2
             for (int i = 0; i<x1_b ; i++){
                 hijo2[conth2] = p2[i];
                 conth2++;
             }
             for (int i = x1; i<x2 ; i++){
                 hijo2[conth2] = p1[i];
                 conth2++;
             }
             for (int i = x2_b; i<p2.length ; i++){
                 hijo2[conth2] = p2[i];
                 conth2++;
             }
 
 
 
             // Se agregan los hijos a la nueva poblacion
             if (validHyp(hijo1)){
                 Ps.add(hijo1);
                 Ps.add(hijo2);
             }
             else{
                 i_cross--;
             }
 
         }
         // Retornamos la nueva poblacion completa
         return Ps;
 
     }
     
     private void mutate(ArrayList<int[]> Ps) {
         int n = (int) (m*Ps.size() / 100);
         Random gen = new Random();
         int indexC;
         int whichBit;
         int[] candidate;
         int[] backup;
         for(int i = 0; i < n; i++) {
             while(true) {
                 indexC = (int) (gen.nextDouble()*(Ps.size()));
                 candidate = Ps.get(indexC);
                 whichBit = (int) (gen.nextDouble()*(candidate.length));
                 backup = candidate.clone();
                 // Check si coincide con método anticonceptivo
                 if (whichBit != 0 && (whichBit % 35 == 0)) {
                     // Modificación especial
                     candidate[whichBit] = (int) (1 + gen.nextDouble()*(3));
                     break;
                 }
                 else {
                     candidate[whichBit] = (candidate[whichBit] == 0) ? 1 : 0;
                     // Verificar que el flip no haya dañado la regla.
                     if (validHyp(candidate)) {
                         break;
                     }
                     else {
                         // Revertir cambios
                         Ps.remove(indexC);
                         Ps.add(backup);
                     }
                 }
             }
         }
     }
     
     private void addAlternative(int[] hyp) {
         Random gen = new Random();
         int which;
         while(true) {
             which = (int) (gen.nextDouble()*(hyp.length));
             if (which != 0 && (which % 35 == 0)) {
                 continue;
             } else {
                 if (hyp[which] == 0) {
                     hyp[which] = 1;
                     break;
                 } else
                     continue;
             }
         }
     }
     
     private void dropCondition(int[] hyp) {
         Random gen = new Random();
         int whichCond = (int) (gen.nextDouble()*(10));
         int nrules = hyp.length / Parser.REP_SIZE;
         int whichRule = (int) (gen.nextDouble()*(nrules));
         int base = 0;
         for(int i = 0; i < whichCond; i++) {
             base += Parser.seq[i];
         }
         for(int i = whichRule*Parser.REP_SIZE + base; 
                 i < whichRule*Parser.REP_SIZE + base + Parser.seq[whichCond];
                 i++) {
             if (hyp[i] == 0) 
                 hyp[i] = 1;
         }
     }
     
     public int[] go() {
         ArrayList<int[]> population = initPop();
         double[] ftn = fitness(population);
        System.out.println("Fitness");
         for(int i = 0; i < ftn.length; i++)
             System.out.print(ftn[i]+" ");
         System.out.println("");
         Pair best;
         while ((best = max(ftn)).getRight() < fitness_threshold) {
             // Crear nueva generación
             printHyp(population.get(best.getLeft()));
             System.out.println("Fitness best = "+best.getRight());
 //            int prop = 500;
             System.out.println("r = "+r);
             int prop = (int) ((1.0-r)*(double) p);
 //            System.out.println("Prop = "+prop2);
             ArrayList<Pair> Pr = computeProbs(ftn);
             System.out.println("Roulette before");
             ArrayList<int[]> Ps = rouletteSelection(prop, population, Pr);
             System.out.println("Roulette after");
             int n_cross = (int) ((r*p)/2);
             System.out.println("Crossover before");
             Ps = crossover(n_cross, population, ftn, Ps, Pr);
             System.out.println("Crossover after");
             System.out.println("Mutate before");
             mutate(Ps);
             System.out.println("Mutate after");
             population = Ps;
             ftn = fitness(population);
 //            System.out.println("NewFitness");
 //            for(int i = 0; i < ftn.length; i++)
 //                System.out.print(ftn[i]+" ");
 //            System.out.println("");
 //            System.out.println("Best in go");
 //            System.out.println(population.get(best.getLeft()));
         }
         return population.get(best.getLeft());
     }
 }
