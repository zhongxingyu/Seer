 package regression_generator.help;
 
 //Нормальное распределение
 // Передаем в конструктор сигму и мат. ожидание
 // метод next возвратит велечину распределенную по нормальному закону.
 public class NormalDistribution {
 
     double sigma;
     double mx;
 
     public NormalDistribution(double m, double sx) {
         mx = m;
         sigma = sx;
     }
 
     public double[] generateArray(int length) {
         double[] result = new double[length];
         for (int i = 0; i < result.length; i++) {
             result[i] = next();
         }
         return result;
     }
 
 
 
     public double next() {
         boolean solved = false;
         double x1 = 0;
         double x2 = 0;
         double normalValue = 0;
         while (!solved) {
             double R1 = Math.random();
             double R2 = Math.random();
             double zeroThree = 1.0 / 3;//0.3(3)
             double zeroSix = 2.0 / 3;//0.6(6)
             if (R1 <= zeroThree) {
                 x1 = -0.5 + Math.log(3 * R1);
             }
             if (R1 > zeroThree && R1 <= zeroSix) {
                 x1 = -1.5 + 3 * R1;
             }
             if (R1 > zeroSix) {
                 x1 = 0.5 - Math.log(3 - 3 * R1);
             }
             if (R2 <= f1(x1) / g(x1)) {
                normalValue = sigma * x1 + mx;
                 solved = true;
             }
         }
         return normalValue;
     }
 
     private double g(double x) {
         double result;
         if (Math.abs(x) > 0.5) {
             result = Math.exp(-Math.abs(x) + 0.5);
         } else {
             result = 1;
         }
         return result / Math.sqrt(Math.PI * 2);
     }
    // Плотность распределения для  нормального распределения (MX,Sigma)
    private double f1(double x){
        return (Math.exp(-Math.pow(x-mx,2)/(2*sigma*sigma)))/(sigma*Math.sqrt((2*Math.PI)));
     }
 }
