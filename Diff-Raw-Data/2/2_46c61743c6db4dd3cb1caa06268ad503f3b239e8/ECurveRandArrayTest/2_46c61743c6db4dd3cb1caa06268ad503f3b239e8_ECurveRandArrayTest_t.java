 package arithmetic.objects.groups;
 
 import java.io.UnsupportedEncodingException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import cryptographic.primitives.HashFuncPRG;
 import cryptographic.primitives.HashFuncPRGRandomOracle;
 import cryptographic.primitives.HashFunction;
 import cryptographic.primitives.PseudoRandomGenerator;
 import cryptographic.primitives.RandomOracle;
 import cryptographic.primitives.SHA2HashFunction;
 
 import algorithms.params.Parameters;
 import arithmetic.objects.*;
 import arithmetic.objects.arrays.ArrayGenerators;
 import arithmetic.objects.arrays.ArrayOfElements;
 import arithmetic.objects.basicelements.Node;
 import arithmetic.objects.basicelements.StringLeaf;
 
 
 public class ECurveRandArrayTest {
 
 	@Test
 	public void TestArr() throws UnsupportedEncodingException {
 		ECurveRandArray one = new ECurveRandArray(new LargeInteger("37"),LargeInteger.ONE,LargeInteger.ONE);
 
 		LargeInteger tst1 = one.shanksTonelli(new LargeInteger("28"));
 		Assert.assertEquals(new LargeInteger("9"), one.getQ());
 		Assert.assertEquals(new LargeInteger("2"), one.getS());
 		Assert.assertEquals(new LargeInteger("18"), tst1);
 
 		ECurveRandArray two = new ECurveRandArray(new LargeInteger("13"),LargeInteger.ONE,LargeInteger.ONE);
 		LargeInteger tst2 = two.shanksTonelli(new LargeInteger("10"));
 		Assert.assertEquals(new LargeInteger("3"), two.getQ());
 		Assert.assertEquals(new LargeInteger("2"), two.getS());
 		Assert.assertEquals(new LargeInteger("6"), tst2);
 
 		LargeInteger q = new LargeInteger("19");
 		Assert.assertEquals(new LargeInteger("3"), q.mod(new LargeInteger("4")));
 		Assert.assertEquals(0,
 				q.mod(new LargeInteger("4")).compareTo(new LargeInteger("3")));
 
 		ECurveRandArray three = new ECurveRandArray(new LargeInteger("19"),LargeInteger.ONE,LargeInteger.ONE);
 		LargeInteger tst3 = three.simpleShanksTonelli(new LargeInteger("11"));
 		Assert.assertEquals(new LargeInteger("9"), three.getQ());
 		Assert.assertEquals(new LargeInteger("1"), three.getS());
 		Assert.assertEquals(new LargeInteger("7"), tst3);
 
 		// Create the array:
 		int nr = 100;
 		IGroup Gq = ElementsExtractor
 				.unmarshal("ECqPGroup(P-256)::0000000002010000001c766572696669636174756d2e61726974686d2e4543715047726f75700100000005502d323536");
 		PseudoRandomGenerator prg = new HashFuncPRG(new SHA2HashFunction(
 				"SHA-256"));
 		HashFunction H = new SHA2HashFunction("SHA-256");
 
 		Parameters params = new Parameters(getClass().getClassLoader()
 				.getResource("protInfo.xml").getFile(), getClass()
 				.getClassLoader().getResource("export/default").getFile(),
				null, "auxsid", 1, false, false, false);
 		Assert.assertNotNull("res is not in the classpath - ask Daniel",
 				getClass().getClassLoader()
 						.getResource("export/default/proofs"));
 
 		Assert.assertTrue(params.fillFromXML());
 		params.fillFromDirectory();
 
 		String s = params.getSessionID() + "." + params.getAuxsid();
 		ByteTree btAuxid = new StringLeaf(s);
 		ByteTree version_proof = new StringLeaf(params.getVersion());
 		ByteTree sGq = new StringLeaf(params.getsGq());
 		ByteTree sPRG = new StringLeaf(params.getsPRG());
 		ByteTree sH = new StringLeaf(params.getSh());
 
 		ByteTree[] input = new ByteTree[5];
 		input[0] = version_proof;
 		input[1] = btAuxid;
 		input[2] = sGq;
 		input[3] = sPRG;
 		input[4] = sH;
 
 		Node node = new Node(input);
 		byte[] Seed = node.toByteArray();
 
 		RandomOracle ROseed = new HashFuncPRGRandomOracle(H, prg.seedlen());
 
 		StringLeaf stringLeaf = new StringLeaf("generators");
 		byte[] independentSeed = ROseed.getRandomOracleOutput(ArrayGenerators
 				.concatArrays(H.digest(Seed), stringLeaf.toByteArray()));
 
 		ECurveGroup G = (ECurveGroup)Gq;
 		
 		ArrayOfElements<IGroupElement> h = Gq.createRandomArray(1000, prg,
 				independentSeed, nr);
 		
 		ECurveGroupElement check;
 		
 		//Check the field order of the point:
 		//Assert.assertEquals(new LargeInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951"), check.getGroup().getFieldOrder());
 		
 		//check one point
 		LargeInteger Q = new LargeInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951");
 		LargeInteger a = new LargeInteger("115792089210356248762697446949407573530086143415290314195533631308867097853948");
 		LargeInteger b = new LargeInteger("41058363725152142129326129780047268409114441015993725554835256314039467401291");
 		ECurveRandArray curve = new ECurveRandArray(Q, a, b);
 		
 		/*Print some points and check their values
 		 * for (int i=0; i<4; i++) {
 			check = (ECurveGroupElement) h.getAt(i);
 			System.out.println("point number "+i);
 			System.out.println("yValue = "+check.getElement().getY().getElement());
 			System.out.println("xValue = "+check.getElement().getX().getElement());
 			System.out.println("zValue = "+curve.f(check.getElement().getX().getElement()));
 			System.out.println();
 		}*/
 		
 		//Check if the points are really on the curve 
 		
 		LargeInteger yValue = new LargeInteger("53939506714489701886456415263120518424983556687449170106546387547120013873082");
 		LargeInteger zValue = new LargeInteger("19353912749743277024464628119578047917971379149422101751139737045657514424033");
 		LargeInteger xValue = new LargeInteger("99231359047137800212806420171596481116912646397664473919112123289570225611325");
 		
 		
 		//y vs. shanksTonelli(z)
 		Assert.assertEquals(yValue, curve.shanksTonelli(zValue));
 		
 		//z vs. y^2 (mod q)
 		Assert.assertEquals(zValue, yValue.modPow(new LargeInteger("2"), Q));
 		
 		//z vs f(xi)
 		Assert.assertEquals(zValue, curve.f(xValue));
 		
 		//y^2 (mod q) vs f(xi)
 		Assert.assertEquals(yValue.modPow(new LargeInteger("2"), Q), curve.f(xValue));
 		
 		//f_xi - the function value
 		LargeInteger f_xi = (xValue.modPow(new LargeInteger("3"), Q).add(xValue.multiply(a)).add(b)).mod(Q);		
 		
 		//f_xi vs y^2
 		Assert.assertEquals(f_xi, yValue.modPow(new LargeInteger("2"), Q));
 		
 		
 	}
 
 
 
 }
