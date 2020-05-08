 package flask.codes;
 
 import flask.codes.checker.CRCChecker;
 import flask.codes.generator.BitPatternSequenceGenerator;
 import flask.codes.generator.CRCGenerator;
 import flask.type.Bit;
 import flask.type.BitPattern;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.List;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class CRCGeneratorTest {
 
 	private BitPattern sampleDataword;
 	private BitPattern sampleDataword2;
 	private BitPattern bookSampleCRCCode0;
 	private BitPattern bookSampleCRCCode1;
 	private BitPattern bookSampleCRCCode2;
 	private BitPattern bookSampleCRCCode3;
 	private BitPattern bookSampleCRCCode4;
 	private BitPattern bookSampleCRCCode5;
 	private BitPattern bookSampleCRCCode6;
 	private BitPattern bookSampleCRCCode7;
 	private BitPattern bookSampleCRCCode8;
 	private BitPattern bookSampleCRCCode9;
 	private BitPattern bookSampleCRCCode10;
 	private BitPattern bookSampleCRCCode11;
 	private BitPattern bookSampleCRCCode12;
 	private BitPattern bookSampleCRCCode13;
 	private BitPattern bookSampleCRCCode14;
 	private BitPattern bookSampleCRCCode15;
 	private Bit[] polynomial = new Bit[]{new Bit(1), new Bit(0), new Bit(1), new Bit(1)};
 	private int crcLength = 3;
 
 	@Before
 	public void setUp() {
 		sampleDataword = new BitPattern(1, 0, 1, 0, 0, 1, 0, 1, 1);
 		sampleDataword2 = new BitPattern(1, 1, 0, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 0);
 
 		bookSampleCRCCode0 = new BitPattern(0, 0, 0, 0, 0, 0, 0);
 		bookSampleCRCCode1 = new BitPattern(0, 0, 0, 1, 0, 1, 1);
 		bookSampleCRCCode2 = new BitPattern(0, 0, 1, 0, 1, 1, 0);
 		bookSampleCRCCode3 = new BitPattern(0, 0, 1, 1, 1, 0, 1);
 		bookSampleCRCCode4 = new BitPattern(0, 1, 0, 0, 1, 1, 1);
 		bookSampleCRCCode5 = new BitPattern(0, 1, 0, 1, 1, 0, 0);
 		bookSampleCRCCode6 = new BitPattern(0, 1, 1, 0, 0, 0, 1);
 		bookSampleCRCCode7 = new BitPattern(0, 1, 1, 1, 0, 1, 0);
 		bookSampleCRCCode8 = new BitPattern(1, 0, 0, 0, 1, 0, 1);
 		bookSampleCRCCode9 = new BitPattern(1, 0, 0, 1, 1, 1, 0);
 		bookSampleCRCCode10 = new BitPattern(1, 0, 1, 0, 0, 1, 1);
 		bookSampleCRCCode11 = new BitPattern(1, 0, 1, 1, 0, 0, 0);
 		bookSampleCRCCode12 = new BitPattern(1, 1, 0, 0, 0, 1, 0);
 		bookSampleCRCCode13 = new BitPattern(1, 1, 0, 1, 0, 0, 1);
 		bookSampleCRCCode14 = new BitPattern(1, 1, 1, 0, 1, 0, 0);
 		bookSampleCRCCode15 = new BitPattern(1, 1, 1, 1, 1, 1, 1);
 	}
 
 	@Test
 	public void crc() {
 		Bit[] crc = CRCGenerator.crc(sampleDataword, polynomial, crcLength);
 		checkSame(crc, 0, 1, 0);
 
 		crc = CRCGenerator.crc(sampleDataword2, polynomial, crcLength);
 		checkSame(crc, 1, 0, 0);
 	}
 
 	private void checkSame(Bit[] bit, int... value) {
 		for (int i = 0; i < bit.length; i++)
 			assertThat(bit[i].value(), is(value[i]));
 	}
 
 	@Test
 	public void sampleGenerateTest() {
 		BitPattern codeword = CRCGenerator.generate(sampleDataword, polynomial, crcLength);
 		checkSame(codeword.values(), 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0);
 	}
 
 	@Test
 	public void bookCRCTest() {
 		List<BitPattern> datawords = BitPatternSequenceGenerator.generate(4); // 0000 ~ 1111
 
 		check(datawords.get(0), bookSampleCRCCode0.toString()); // "0000000"
 		check(datawords.get(1), bookSampleCRCCode1.toString()); // "0001011"
 		check(datawords.get(2), bookSampleCRCCode2.toString()); // "0010110"
 		check(datawords.get(3), bookSampleCRCCode3.toString()); // "0011101"
 		check(datawords.get(4), bookSampleCRCCode4.toString()); // "0100111"
 		check(datawords.get(5), bookSampleCRCCode5.toString()); // "0101100"
 		check(datawords.get(6), bookSampleCRCCode6.toString()); // "0110001"
 		check(datawords.get(7), bookSampleCRCCode7.toString()); // "0111010"
 		check(datawords.get(8), bookSampleCRCCode8.toString()); // "1000101"
 		check(datawords.get(9), bookSampleCRCCode9.toString()); // "1001110"
 		check(datawords.get(10), bookSampleCRCCode10.toString()); // "1010011"
 		check(datawords.get(11), bookSampleCRCCode11.toString()); // "1011000"
 		check(datawords.get(12), bookSampleCRCCode12.toString()); // "1100010"
 		check(datawords.get(13), bookSampleCRCCode13.toString()); // "1101001"
 		check(datawords.get(14), bookSampleCRCCode14.toString()); // "1110100"
 		check(datawords.get(15), bookSampleCRCCode15.toString()); // "1111111"
 
 	}
 
 	private void check(BitPattern dataword, String booksCode) {
 		assertThat(CRCGenerator.generate(dataword, polynomial, crcLength).toString(), is(booksCode));
 	}
 
 	@Test
 	public void checkCRC() {
 		assertThat(CRCChecker.check(bookSampleCRCCode0, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode1, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode2, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode3, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode4, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode5, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode6, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode7, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode8, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode9, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode10, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode11, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode12, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode13, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode14, polynomial, crcLength), is(true));
 		assertThat(CRCChecker.check(bookSampleCRCCode15, polynomial, crcLength), is(true));
 	}
 
 	@Test
 	public void failingCheckTest() {
 		assertThat(CRCChecker.check(new BitPattern(0, 0, 1, 0, 1, 1, 1), polynomial, crcLength), is(false));
 	}
 
 }
