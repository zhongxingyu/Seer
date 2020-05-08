 /**
  *
  */
 package features.mouse;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import features.mouse.Calculator;
 
 /**
  * @author reimei
  *
  */
 public class CalculatorTest {
 
 	Calculator target;
 
 	@Before
 	public void setUp() throws Exception {
 		target = new Calculator();
 	}
 
 	// 計算機。 数式は正の整数、演算子一つ、四則演算のみとする。 小数点以下が続く場合、第四位を四捨五入する。
 
 	/**
 	 * 複数演算子
 	 */
 	@Test(expected = java.lang.IllegalArgumentException.class)
 	public void 複数演算子(){
 		target.execute("1+2+3");
 	}
 
 	/**
 	 * 数字と四則演算子以外：英字
 	 */
 	@Test(expected = java.lang.IllegalArgumentException.class)
 	public void 複数演算子_数字と四則演算子以外_英字(){
 		target.execute("1+A");
 	}
 
 	/**
 	 * 数字と四則演算子以外：空白
 	 */
 	@Test(expected = java.lang.IllegalArgumentException.class)
 	public void 複数演算子_数字と四則演算子以外_空白(){
 		target.execute("1+ A");
 	}
 
 	@Test(expected = java.lang.IllegalArgumentException.class)
 	public void 複数演算子_数字と四則演算子以外_記号(){
 		target.execute("1&2");
 	}
 
 
 	@Test(expected = java.lang.IllegalArgumentException.class)
 	public void 左辺ゼロ(){
 		target.execute("0+1");
 	}
 
 	@Test(expected = java.lang.IllegalArgumentException.class)
 	public void 右辺ゼロ(){
 		target.execute("1+0");
 	}
 
 	@Test(expected = java.lang.IllegalArgumentException.class)
 	public void 負数で始まる(){
 		target.execute("-1+1");
 	}
 
 	@Test
 	public void 足し算(){
 		assertEquals("2", target.execute("1+1"));
 	}
 
 	@Test
 	public void 引き算(){
 		assertEquals("88", target.execute("100-12"));
 	}
 
 	@Test
 	public void 掛け算(){
 		assertEquals("600", target.execute("20*30"));
 	}
 
 	@Test
 	public void 割り算(){
 		assertEquals("3", target.execute("99/33"));
 	}
 
 	@Test
 	public void 割り算_小数第一位(){
 		assertEquals("1.5", target.execute("3/2"));
 	}
 
 	@Test
 	public void 割り算_小数第二位(){
 		assertEquals("0.25", target.execute("1/4"));
 	}
 
 	@Test
 	public void 割り算_小数第三位(){
 		assertEquals("0.125", target.execute("1/8"));
 	}
 
 	@Test
 	public void 割り算_小数第四位_五入(){
 		assertEquals("0.063", target.execute("1/16"));
 	}
 	@Test
 	public void 割り算_小数第四位_四捨(){
 		assertEquals("0.071", target.execute("1/14"));
 	}
 
 }
