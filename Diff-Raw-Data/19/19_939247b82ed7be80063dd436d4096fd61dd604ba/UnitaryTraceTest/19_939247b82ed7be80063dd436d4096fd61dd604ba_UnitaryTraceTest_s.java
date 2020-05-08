 package cc.redberry.physics.feyncalc;
 
 import cc.redberry.core.TAssert;
 import cc.redberry.core.context.CC;
 import cc.redberry.core.indices.IndexType;
 import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
 import cc.redberry.core.tensor.Tensor;
 import cc.redberry.core.transformations.RemoveDueToSymmetry;
 import org.junit.Test;
 
 import static cc.redberry.core.tensor.Tensors.*;
 
 /**
  * @author Dmitry Bolotin
  * @author Stanislav Poslavsky
  */
 public class UnitaryTraceTest {
 
     @Test
     public void test1() {
         GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
         CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
         indicesInsertion.addInsertionRule(parseSimple("T^a'_b'a"), IndexType.LatinLower1);
 
         Tensor t = parse("Tr[T_a*T_b]");
         t = UnitaryTrace.unitaryTrace(t);
         TAssert.assertEquals(t, parse("g_ab/2"));
     }
 
     @Test
     public void test2() {
         GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
         CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
         indicesInsertion.addInsertionRule(parseSimple("T^a'_b'a"), IndexType.LatinLower1);
 
         setSymmetric(parseSimple("d_abd"));
         addAntiSymmetry(parseSimple("f_abc"), 1, 0, 2);
         addSymmetry("f_abc", 2, 0, 1);
         Tensor t = parse("Tr[T_a*T_b*T_c]");
         t = UnitaryTrace.unitaryTrace(t);
         t = RemoveDueToSymmetry.INSTANCE.transform(t);
         Tensor expected = parse("d_abc/4+I/4*f_abc");
         TAssert.assertEquals(t, expected);
     }
 
 
     @Test
     public void test3() {
         GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
         CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
         indicesInsertion.addInsertionRule(parseSimple("T^a'_b'a"), IndexType.LatinLower1);
 
         setSymmetric(parseSimple("d_abd"));
         addAntiSymmetry(parseSimple("f_abc"), 1, 0, 2);
         addSymmetry("f_abc", 2, 0, 1);
         Tensor t = parse("Tr[T_a*T_b*T_c*T_d]");
         Tensor t1 = parse("Tr[T_b*T_a*T_c*T_d]");
 
         t = UnitaryTrace.unitaryTrace(t);
         t1 = UnitaryTrace.unitaryTrace(t1);
         System.out.println(subtract(t, t1));
 
         t = RemoveDueToSymmetry.INSTANCE.transform(t);
         Tensor expected = parse("-(I/8)*f_adx*d_bc^x + (I/8)*d_adx*f_bc^x+1/8*d_ade*d_bc^e - 1/8*d_bde*d_ac^e+1/8*d_cde*d_ab^e + 1/(4*N)*g_ad*g_bc - 1/(4*N)*g_ac*g_bd + 1/(4*N)*g_ab*g_cd");
         System.out.println(t);
         System.out.println(expected);
        TAssert.assertEquals(t, expected);
     }
 }
