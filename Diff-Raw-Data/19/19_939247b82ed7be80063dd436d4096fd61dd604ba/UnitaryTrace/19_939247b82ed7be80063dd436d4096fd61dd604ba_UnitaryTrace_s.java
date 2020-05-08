 package cc.redberry.physics.feyncalc;
 
 import cc.redberry.core.indexgenerator.IndexGenerator;
 import cc.redberry.core.indices.IndexType;
 import cc.redberry.core.indices.IndicesFactory;
 import cc.redberry.core.indices.SimpleIndices;
 import cc.redberry.core.number.Complex;
 import cc.redberry.core.tensor.*;
 import cc.redberry.core.tensor.iterator.TensorLastIterator;
 import cc.redberry.core.transformations.ContractIndices;
 import cc.redberry.core.transformations.Transformation;
 import cc.redberry.core.transformations.expand.Expand;
 import cc.redberry.core.utils.ArraysUtils;
 import cc.redberry.core.utils.TensorUtils;
 
 import java.util.*;
 
 import static cc.redberry.core.tensor.Tensors.*;
 
 /**
  * @author Dmitry Bolotin
  * @author Stanislav Poslavsky
  */
 public final class UnitaryTrace implements Transformation {
     private final SimpleTensor SuN, f, d;
     private final Tensor N;
 
     public UnitaryTrace(SimpleTensor suN, SimpleTensor f, SimpleTensor d, Tensor n) {
         SuN = suN;
         this.f = f;
         this.d = d;
         N = n;
     }
 
     @Override
     public Tensor transform(Tensor t) {
         return unitaryTrace(t, SuN, f, d, N);
     }
 
     public static final Tensor unitaryTrace(Tensor t) {
         return unitaryTrace(t, parseSimple("T^a'_b'a"), parseSimple("f_abc"), parseSimple("d_abc"), parseSimple("N"));
     }
 
     public static final Tensor unitaryTrace(Tensor tensor, SimpleTensor SuN, SimpleTensor f, SimpleTensor d, Tensor N) {
         IndexType[] types = TraceUtils.extractTypesFromMatrix(SuN);
         TensorLastIterator iterator = new TensorLastIterator(tensor);
         Tensor c;
         while ((c = iterator.next()) != null) {
             if (c instanceof SimpleTensor) {
                 if (((SimpleTensor) c).getName() == SuN.getName() && c.getIndices().getOfType(types[1]).getFree().size() == 0)
                     iterator.set(Complex.ZERO);
             } else if (c instanceof Product) {
                 TensorBuilder suns = new ProductBuilder();
                 boolean containsSun = false;
                 for (int i = c.size() - 1; i >= 0; --i) {
                     if (isSuN(c.get(i), SuN.getName())) {
                         containsSun = true;
                         suns.put(c.get(i));
                        if (c instanceof Product)
                            c = ((Product) c).remove(i);
                         else {
                             assert i == 0;
                            c = Complex.ONE;
                         }
                     }
                 }
                 if (!containsSun)
                     continue;
                Tensor temp = multiply(c, traceOfProduct(suns.build(), SuN.getName(), f.getName(), d.getName(), N, types[0], types[1]));
                 temp = Expand.expand(temp, ContractIndices.ContractIndices);
                 temp = ContractIndices.contract(temp);
                 iterator.set(temp);
             }
         }
         return iterator.result();
     }
 
     private static Tensor traceOfProduct(Tensor tensor,
                                          int sunName, int fName, int dName, Tensor N,
                                          IndexType metricType, IndexType matrixType) {
         Expression[] subs = getSubstitutions(sunName, fName, dName, N, metricType, matrixType);
         Transformation[] transformations = new Transformation[]{ContractIndices.ContractIndices};
         transformations = ArraysUtils.addAll(transformations, Arrays.copyOfRange(subs, 1, subs.length));
 
         Tensor oldTensor = tensor, newTensor;
         while (true) {
             newTensor = subs[0].transform(oldTensor);
             newTensor = Expand.expand(newTensor, transformations);
             for (Transformation tr : transformations)
                 newTensor = tr.transform(newTensor);
             if (newTensor == oldTensor)
                 break;
             oldTensor = newTensor;
         }
 
         return newTensor;
     }
 
     private static final boolean isSuN(Tensor tensor, int sunName) {
         return tensor instanceof SimpleTensor && ((SimpleTensor) tensor).getName() == sunName;
     }
 
     private static final class CacheContainer {
         final int sunName, fName, dName;
         final Tensor N;
 
         private CacheContainer(int sunName, int fName, int dName, Tensor n) {
             this.sunName = sunName;
             this.fName = fName;
             this.dName = dName;
             this.N = n;
         }
 
         @Override
         public boolean equals(Object o) {
             CacheContainer that = (CacheContainer) o;
             return sunName == that.sunName &&
                     fName == that.sunName &&
                     dName == that.dName &&
                     TensorUtils.equals(N, that.N);
         }
 
         @Override
         public int hashCode() {
             int result = sunName;
             result = 31 * result + fName;
             result = 31 * result + dName;
             result = 31 * result + N.hashCode();
             return result;
         }
 
     }
 
     //todo static expression vs CC.resetTensorNames
     private static final Map<CacheContainer, Expression[]> cachedSubstitutions = new HashMap<>();
 
     private static Expression[] getSubstitutions(int sunName, int fName, int dName, Tensor N,
                                                  IndexType metricType, IndexType matrixType) {
         CacheContainer cacheContainer = new CacheContainer(sunName, fName, dName, N);
         Expression[] expressions = cachedSubstitutions.get(cacheContainer);
         if (expressions != null)
             return expressions;
         List<Expression> expressionsList = new ArrayList<>();
         IndexGenerator generator = new IndexGenerator();
 
         //creating  T_a*T_b  = 1/2N g_ab + I/2*f_abc*T^c + 1/2*d_abc*T^c
         int upper, lower, contracted, firstMetric, secondMetric, thirdMetric;
         SimpleIndices indicesOfA = IndicesFactory.createSimple(null,
                 firstMetric = generator.generate(metricType),
                 upper = (0x80000000 | generator.generate(matrixType)),
                 contracted = generator.generate(matrixType));
 
         SimpleIndices indicesOfB = IndicesFactory.createSimple(null,
                 secondMetric = generator.generate(metricType),
                 (0x80000000 | contracted),
                 lower = generator.generate(matrixType));
 
         SimpleIndices indicesOfC = IndicesFactory.createSimple(null,
                 (0x80000000 | (thirdMetric = generator.generate(metricType))),
                 upper, lower);
 
 
         Tensor lhs = multiply(simpleTensor(sunName, indicesOfA),
                 simpleTensor(sunName, indicesOfB));
 
         //(1/(2*N))*g_ab
         Tensor gTerm = multiply(Complex.ONE_HALF, reciprocal(N),
                 createMetric(firstMetric, secondMetric), createKronecker(upper, lower));
 
         SimpleIndices fIndices = IndicesFactory.createSimple(null, firstMetric, secondMetric, thirdMetric);
         Tensor C = simpleTensor(sunName, indicesOfC);
         //(I/2)*f_abc*T^c
         Tensor fTerm = multiply(Complex.ONE_HALF, Complex.IMAGE_ONE,
                 simpleTensor(fName, fIndices), C);
         //(1/2)*d_abc*T^c
         Tensor dTerm = multiply(Complex.ONE_HALF,
                 simpleTensor(dName, fIndices), C);
 
         expressionsList.add(expression(lhs, sum(gTerm, fTerm, dTerm)));
 
         //creating Tr[T] = 0
         indicesOfA = IndicesFactory.createSimple(null, firstMetric, (0x80000000 | contracted), contracted);
         expressionsList.add(expression(simpleTensor(sunName, indicesOfA), Complex.ZERO));
 
         //creating d_apq*d_b^pq = (N**2 - 4)/N * g_ab
         indicesOfA = IndicesFactory.createSimple(null,
                 upper = generator.generate(metricType),
                 firstMetric = generator.generate(metricType),
                 secondMetric = generator.generate(metricType));
         indicesOfB = IndicesFactory.createSimple(null,
                 lower = generator.generate(metricType),
                 (0x80000000 | firstMetric),
                 (0x80000000 | secondMetric));
 
         lhs = multiply(simpleTensor(dName, indicesOfA), simpleTensor(dName, indicesOfB));
         Tensor rhs = multiply(reciprocal(N), subtract(pow(N, 2), Complex.FOUR), createMetric(upper, lower));
         expressionsList.add(expression(lhs, rhs));
 
         //creating f_apq*f_b^pq = N * g_ab
         lhs = multiply(simpleTensor(fName, indicesOfA), simpleTensor(fName, indicesOfB));
         rhs = multiply(N, createMetric(upper, lower));
         expressionsList.add(expression(lhs, rhs));
 
         //creating f_apq*d_b^pq = 0
         lhs = multiply(simpleTensor(fName, indicesOfA), simpleTensor(dName, indicesOfB));
         expressionsList.add(expression(lhs, Complex.ZERO));
 
 
         //creating Tr[1] = N
         lhs = createKronecker(contracted, (0x80000000 | contracted));
         expressionsList.add(expression(lhs, N));
 
         expressions = expressionsList.toArray(new Expression[expressionsList.size()]);
         cachedSubstitutions.put(cacheContainer, expressions);
         return expressions;
     }
 }
