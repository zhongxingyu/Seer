 package arithmetic.objects.arrays;
 
 import java.io.UnsupportedEncodingException;
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import arithmetic.objects.LargeInteger;
 import arithmetic.objects.field.IntegerFieldElement;
 import arithmetic.objects.field.PrimeOrderField;
 import arithmetic.objects.groups.IGroupElement;
 import arithmetic.objects.groups.ModGroup;
 import arithmetic.objects.groups.ModGroupElement;
 import arithmetic.objects.groups.ProductGroupElement;
 import cryptographic.primitives.CryptoUtils;
 
 /**
  * Tests for ArrayOfElements class.
  * 
  * @author Daniel
  * 
  */
 public class ArrayOfElementsTests {
 
 	private ArrayOfElements<IntegerFieldElement> array = new ArrayOfElements<IntegerFieldElement>();
 	private PrimeOrderField field = new PrimeOrderField(new LargeInteger("4"));
 	private IntegerFieldElement element3 = new IntegerFieldElement(
 			new LargeInteger("3"), field);
 	private IntegerFieldElement element4 = new IntegerFieldElement(
 			new LargeInteger("4"), field);
 
 	@Test
 	public void getAtTest() {
 		array.add(element3);
 		array.add(element4);
 		Assert.assertTrue(element3.equals(array.getAt(0)));
 		Assert.assertTrue(element4.equals(array.getAt(1)));
 	}
 
 	@Test
 	public void setAtTest() {
 		array.add(element3);
 		Assert.assertTrue(element3.equals(array.getAt(0)));
 		array.setAt(0, element4);
 		Assert.assertTrue(element4.equals(array.getAt(0)));
 	}
 
 	@Test
 	public void addTest() {
 		array.add(element3);
 		Assert.assertEquals(1, array.getSize());
 	}
 
 	@Test
 	public void getSizeTest() {
 		Assert.assertEquals(0, array.getSize());
 		array.add(element3);
 		Assert.assertEquals(1, array.getSize());
 		array.add(element4);
 		Assert.assertEquals(2, array.getSize());
 	}
 
 	@Test
 	public void equalsTest() {
 		ArrayOfElements<IntegerFieldElement> newArray = new ArrayOfElements<IntegerFieldElement>();
 		Assert.assertTrue(newArray.equals(array));
 		array.add(element3);
 		Assert.assertFalse(newArray.equals(array));
 		newArray.add(element3);
 		Assert.assertTrue(newArray.equals(array));
 	}
 
 	@Test
 	public void toByteArrayTest() throws UnsupportedEncodingException {
 		array.add(element3);
 		array.add(element4);
 		Assert.assertEquals("0000000002" + "010000000103" + "010000000100",
 				CryptoUtils.bytesToHexString(array.toByteArray()));
 		
 		
 		ModGroup ring = new ModGroup(new LargeInteger("263"),
 				new LargeInteger("131"), new LargeInteger("2"));
 
 		IGroupElement one = new ModGroupElement(
 				new LargeInteger("1"), ring);
 		IGroupElement two = new ModGroupElement(
 				new LargeInteger("2"), ring);
 		IGroupElement three = new ModGroupElement(
 				new LargeInteger("3"), ring);
 		IGroupElement four = new ModGroupElement(
 				new LargeInteger("4"), ring);
 		IGroupElement five = new ModGroupElement(
 				new LargeInteger("5"), ring);
 		IGroupElement six = new ModGroupElement(
 				new LargeInteger("6"), ring);
 		IGroupElement seven = new ModGroupElement(
 				new LargeInteger("7"), ring);
 		IGroupElement eight = new ModGroupElement(
 				new LargeInteger("8"), ring);
 		ArrayOfElements<IGroupElement> arr1 = new ArrayOfElements<IGroupElement>();
 		ArrayOfElements<IGroupElement> arr2 = new ArrayOfElements<IGroupElement>();
 		ArrayOfElements<IGroupElement> arr3 = new ArrayOfElements<IGroupElement>();
 		ArrayOfElements<IGroupElement> arr4 = new ArrayOfElements<IGroupElement>();
 		arr1.add(one); arr1.add(four);
 		arr2.add(two); arr2.add(five);
 		arr3.add(three); arr3.add(six);
 		arr4.add(seven); arr4.add(eight);
 		
 		ProductGroupElement pre1 = new ProductGroupElement(arr1);
 		ProductGroupElement pre2 = new ProductGroupElement(arr2);
 		ProductGroupElement pre3 = new ProductGroupElement(arr3);
 		ProductGroupElement pre4 = new ProductGroupElement(arr4);
 		
 		ArrayOfElements<ProductGroupElement> arr = new ArrayOfElements<ProductGroupElement>();
 		arr.add(pre1); arr.add(pre2); arr.add(pre3);
 		Assert.assertEquals("000000000200000000030100000002000101000000020002010000000200030000000003010000000200040100000002000501000000020006",
 				CryptoUtils.bytesToHexString(arr.toByteArray()));
 		
 		ProductGroupElement pre5 = new ProductGroupElement(pre1, pre2);
 		ProductGroupElement pre6 = new ProductGroupElement(pre3, pre4);
 		ArrayOfElements<ProductGroupElement> arrr = new ArrayOfElements<ProductGroupElement>();
 		arrr.add(pre5);
 		arrr.add(pre6);
		Assert.assertEquals("00000000020000000002000000000201000000020001010000000200030000000002010000000200040100000002000600000000020000000002010000000200020100000002000700000000020100000002000501000000020008",
 				CryptoUtils.bytesToHexString(arrr.toByteArray()));
 	}
 }
