 /**
  * Задача - посчитать сумму чётных чисел Фибоначчи, не превышающих заданное число. 
  * 
  * Три варианта решения.
  * В кратце - первые два реализовывают тривиальный алгоритм.
  * Отличие состоит в том, что первый вариант быстро пишется хардкодом (спортивное программирование),
  * а второй использует класс Matrix и близок к стилю прикладного (а не спортивного программирования).
  *
  * Третий же вариант реализует идею вычисления по выведенной аналитической формуле.
  * Более быстр - O( log(n) ), (если более точно O( log(3*n) )
  * где n наибольшее из чисел k, которые удовлетворяют неравенству fib(3*k) <= MAX_VALUE
  *
  * Не рассматривал длинную арифметику, полагал, что ответ с промежуточными вычислениями
  * укладывает в тип java.lang.Long
  */
 
 import java.io.PrintWriter;
 import java.util.Arrays;
 
 public class Task1 {
     public static PrintWriter out;
 
     public static void main(String[] args) {
		out = new PrintWriter(System.out);
         out.println(Simple.getAnswer(3000L));
         out.println(Simple.getAnswer(11000L));
         out.println(Simple.getAnswer(InputData.MAX_VALUE));
         out.println(Solver.getAnswer(3000L));
         out.println(Solver.getAnswer(11000L));
         out.println(Solver.getAnswer(InputData.MAX_VALUE));
         out.println(AnaliticalFormula.getAnswer(3000L));
         out.println(AnaliticalFormula.getAnswer(11000L));
         out.println(AnaliticalFormula.getAnswer(InputData.MAX_VALUE));
     }
 }
 
 class InputData {
     public static final long MAX_VALUE = 4 * 1000 * 1000;
 }
 
 class Simple {
     /**
      * Тривиальный алгоритм. Явно перебираем всё чётные числа Фибоначчи и считаем их сумму.
      * На каком-нибудь контесте или олимпиаде я бы писал именно его, если бы он удовлетворял ограничениям.
      *
      * Заметим, что чётными числами Фибоначчи являются fib(3*k), k = 0,1,2.. inf
      * На основании
      * (1 1) ^n    ( fib(n+1)  fib(n)   )
      * (   )     = (                    )
      * (1 0)       ( fib(n)    fib(n-1) )
      *
      * Можно подсчитывать не все числа Фибоначчи, а делать быстрые переходы к числам вида 3*k:
      *                           (1 1) ^3
      * ( fib(3*k+1) fib(3*k) ) * (   )     = ( fib(3*k + 4) fib(3*k + 3) )
      *                           (1 0)
      *  Раскрывая матричное умножение получаем формулы перехода:
      *  fib(3*k + 4) = 3*fib(3*k+1) + 2*fib(3*k)
      *  fib(3*k + 3) = 2*fib(3*k+1) + fib(3*k)
      *
      * @param maxValue
      * @return сумму чётных чисел Фибоначчи меньших @code{maxValue}
      */
     public static long getAnswer(final long maxValue) {
         long f1 = 1, f0 = 0;
         long sum = 0;
         while(f1 <= maxValue) {
             long t = 3*f1 + 2*f0;
             f0 = 2*f1 + f0;
             f1 = t;
             sum += f0;
         }
         return sum;
     }
 }
 
 /**
  * Алгоритм совпадает с предыдущим.
  * Данный вариант ближе к прикладному программированию.
  */
 class Solver {
     static final Matrix FIB_START = new Matrix(new long[][] {new long[] {1L, 0L}}, 2);
     static final Matrix FIB_MATRIX = new Matrix(new long[][] {new long[] {1L, 1L}, new long[] {1L, 0L}}, 2);
 
     public static long getAnswer(final long maxValue) {
         return produce(FIB_START, FIB_MATRIX.pow(3), maxValue);
     }
 
     public static long produce(Matrix fib, Matrix multiplyMatrix, final long maxValue) {
         long sum = 0;
         long element;
         while ((element = fib.getElement(0, 1)) <= maxValue) {
             sum += element;
             fib = fib.multiply(multiplyMatrix);
         }
         return sum;
     }
 }
 
 /**
  * Ответом на поставленную задачу является
  * sum(k=0..n) {fib(3*k}
  * где n = sup {k | fib(3*k) <= MAX_VALUE}
  * Иначе, n - наибольшее из чисел k, которые удовлетворяют неравенству fib(3*k) <= MAX_VALUE.
  *
  * Данную сумму можно предствить в виде:
  * sum(k=0..n) {fib(3*k} = (1/4) * [ fib(3n + 3) + fib(3n) - 2 ]
  *
  * Сложностью этого подхода является лишь вычисление n по заданному MAX_VALUE
  */
 class AnaliticalFormula {
 
     static Matrix[] fibPowerOfTwo;
 
     private static final int MAX_N_POW = 6;
 
     static  {
         fibPowerOfTwo = new Matrix[MAX_N_POW];
         fibPowerOfTwo[0] = Solver.FIB_MATRIX;
         for(int i = 1; i < MAX_N_POW; ++i) {
             fibPowerOfTwo[i] = fibPowerOfTwo[i - 1].multiply(fibPowerOfTwo[i - 1]);
         }
     }
 
     /**
      * Поиск матрицы с наибольшим четным числом Фибоначчи <= maxValue.
      * Идея схожа с бинарным поиском.
      *
      * @param start стартовая точка для бин. поиска
      * @param step шаг бин. поиска - FIB_MATRIX^2^2^step
      * @param maxValue
      * @return матрицу с наибольшим четным числом Фибоначчи <= maxValue
      */
     public static Matrix getLowerBoundFibonacciMatrix(Matrix start, int step, final long maxValue) {
         Matrix mul = fibPowerOfTwo[step];
         Matrix eval = start;
         Matrix last = eval;
         while (eval.getElement(0, 0) <= maxValue) {
             last = eval;
             eval = eval.multiply(mul);
         }
         if(eval.getElement(1, 0) <= maxValue) {
             return eval;
         } else {
             return getLowerBoundFibonacciMatrix(last, step - 1, maxValue);
         }
     }
 
     public static long getAnswer(final long maxValue) {
         Matrix m = getLowerBoundFibonacciMatrix(Solver.FIB_MATRIX, MAX_N_POW - 1, maxValue).multiply(Solver.FIB_MATRIX);
         return (m.getElement(0, 0) + m.getElement(0, 1) + m.getElement(1, 1) - 2)/4;
     }
 }
 
 class Pair <E, T> {
     E first;
     T second;
     Pair(E first, T second) {
         this.first = first;
         this.second = second;
     }
 
     E first() { return first; }
     T second() { return second; }
 }
 
 /**
  * Простейшая реализация, без параметризации, сложных конструктов и прямого доступа/манипуляции строк и столбцов.
  */
 class Matrix {
     long[][] elements;
     int rowCount;
     int columnCount;
 
     /**
      *
      * @param a элементы новой матрицы
      * @param columnCount количество столбцов. В принципе, можно на вход подать и ступененчатый(зубчатый) массив.
      */
     Matrix(long[][] a, int columnCount) {
         this.rowCount = a.length;
         this.columnCount = columnCount;
         elements = new long[rowCount][];
         for(int i = 0; i < elements.length; ++i) {
             elements[i] = Arrays.copyOf(a[i], columnCount);
         }
     }
 
     long getElement(int row, int column) {
         return elements[row][column];
     }
 
     /**
      * Тривиальное умножение за O(n^3) арифмитечиских операций.
      * @param that
      * /@return матрицу B равную произведению данной матрицы на матрицу that
      */
     Matrix multiply(Matrix that) {
         if(this.columnCount != that.rowCount) {
             //нужно бросить исключению. Но не буду усложнять логику, ибо
             //придётся добавлять catch-блоки, а также реализовывать класс исключения
             return null;
         }
         long[][] newMatrix = new long[this.rowCount][that.columnCount];
         for(int i = 0; i < this.rowCount; ++i) {
             for(int j = 0; j < that.columnCount; ++j) {
                 long ceilValue = 0;
                 for(int k = 0; k < this.columnCount; ++k) {
                     ceilValue += this.elements[i][k] * that.elements[k][j];
                 }
                 newMatrix[i][j] = ceilValue;
             }
         }
         return new Matrix(newMatrix, that.columnCount);
     }
 
     /**
      * Бинарное возведение в степень за O(log(n))
      * @param n натуральная степень
      * @return новую матрицу B = this^n
      */
     Matrix pow(int n) {
         Matrix result = new Matrix(this.elements, this.columnCount);
         Matrix copy = new Matrix(this.elements, this.columnCount);
         while (n > 0) {
             if (n % 2 == 1) {
                 result = result.multiply(this);
                 --n;
             } else {
                 copy = copy.multiply(copy);
                 n >>= 1;
             }
         }
         return result;
     }
 }
