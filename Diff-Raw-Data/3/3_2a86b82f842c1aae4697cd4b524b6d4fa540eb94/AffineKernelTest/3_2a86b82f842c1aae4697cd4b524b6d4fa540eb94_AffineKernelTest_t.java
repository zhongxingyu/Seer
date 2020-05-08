 package math.nyx.codecs;
 
 import static org.junit.Assert.assertEquals;
 import math.nyx.affine.AffineKernel;
 import math.nyx.affine.AffineTransform;
 import math.nyx.framework.SignalBlock;
 import math.nyx.framework.square.SquareDecimationStrategy;
 import math.nyx.utils.TestUtils;
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.RealMatrix;
 import org.apache.commons.math.linear.SparseRealMatrix;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
 public class AffineKernelTest {
	private AffineKernel affineKernel = new AffineKernel();
 
 	@Autowired
 	private SquareDecimationStrategy decimationStrategy;
 
 	@Test
 	public void getAffineTransformForIdenticalBlocks() {
 		verifyAffineTransform(TestUtils.generateSignal(4), TestUtils.generateSignal(4), 0.0, 1.0, 0.0);
 	}
 
 	@Test
 	public void getAffineTransformForBlocksWithScaleAndOffset() {
 		int signalDimension = 4;
 		double scale = 7.0f;
 		double offset = 11.0f;
 
 		RealMatrix domain = TestUtils.generateSignal(signalDimension);
 		RealMatrix range = TestUtils.generateSignal(signalDimension);
 		range = range.scalarMultiply(scale);
 		range = range.scalarAdd(offset);
 
 		verifyAffineTransform(domain, range, 0.0, scale, offset);
 	}
 
 	private void verifyAffineTransform(RealMatrix domain, RealMatrix range,
 			double expectedDistance, double expectedScale, double expectedOffset) {
 		SignalBlock domainBlock = new SignalBlock(0, domain);
 		SignalBlock rangeBlock = new SignalBlock(0, range);
 
 		AffineTransform transform = affineKernel.encode(domainBlock, rangeBlock);
 		assertEquals(expectedDistance, transform.getDistance(), TestUtils.DELTA);
 		assertEquals(transform.toString(), expectedScale, transform.getScale(), TestUtils.DELTA);
 		assertEquals(expectedOffset, transform.getOffset(), TestUtils.DELTA);
 	}
 
 	@Test
 	public void verifyTransform() {
 		double rangeVector[] = new double[] {
 			90.0, 90.0, 89.0, 92.0, 92.0, 90.0, 89.0, 86.0,
 			93.0, 92.0, 89.0, 92.0, 93.0, 88.0, 89.0, 87.0,
 			92.0, 89.0, 90.0, 90.0, 91.0, 89.0, 87.0, 87.0,
 			90.0, 86.0, 89.0, 89.0, 90.0, 89.0, 86.0, 82.0,
 			85.0, 88.0, 88.0, 87.0, 89.0, 86.0, 87.0, 85.0,
 			86.0, 88.0, 86.0, 84.0, 87.0, 87.0, 85.0, 86.0,
 			87.0, 87.0, 87.0, 86.0, 87.0, 87.0, 86.0, 87.0,
 			89.0, 88.0, 86.0, 88.0, 86.0, 86.0, 87.0, 86.0
 		};
 		
 		double domainVector[] = new double[] {
 			51.0, 43.0, 54.0, 59.0, 56.0, 56.0, 52.0, 56.0, 55.0, 55.0, 55.0, 59.0, 60.0, 29.0, 16.0, 33.0,
 			43.0, 50.0, 56.0, 57.0, 54.0, 53.0, 54.0, 57.0, 58.0, 55.0, 57.0, 62.0, 55.0, 24.0, 16.0, 31.0,
 			51.0, 60.0, 57.0, 51.0, 50.0, 52.0, 53.0, 55.0, 56.0, 58.0, 60.0, 56.0, 47.0, 23.0, 15.0, 25.0,
 			52.0, 54.0, 54.0, 51.0, 49.0, 48.0, 53.0, 51.0, 53.0, 58.0, 59.0, 54.0, 44.0, 22.0, 14.0, 19.0,
 			51.0, 50.0, 48.0, 49.0, 48.0, 46.0, 48.0, 51.0, 54.0, 56.0, 52.0, 46.0, 41.0, 21.0, 13.0, 14.0,
 			54.0, 48.0, 47.0, 46.0, 47.0, 44.0, 46.0, 51.0, 48.0, 49.0, 47.0, 42.0, 31.0, 16.0, 11.0, 10.0,
 			48.0, 44.0, 41.0, 38.0, 39.0, 38.0, 40.0, 43.0, 44.0, 40.0, 38.0, 33.0, 25.0, 14.0, 12.0, 8.0,
 			27.0, 25.0, 27.0, 28.0, 28.0, 32.0, 32.0, 30.0, 32.0, 30.0, 27.0, 25.0, 22.0, 15.0, 12.0, 9.0,
 			16.0, 19.0, 23.0, 23.0, 23.0, 21.0, 21.0, 23.0, 24.0, 26.0, 24.0, 24.0, 22.0, 21.0, 15.0, 10.0,
 			8.0, 9.0, 11.0, 12.0, 12.0, 13.0, 13.0, 17.0, 17.0, 21.0, 20.0, 19.0, 20.0, 25.0, 20.0, 14.0,
 			10.0, 8.0, 9.0, 11.0, 10.0, 9.0, 9.0, 9.0, 10.0, 12.0, 12.0, 13.0, 16.0, 17.0, 19.0, 20.0,
 			20.0, 14.0, 11.0, 10.0, 11.0, 11.0, 10.0, 10.0, 10.0, 9.0, 10.0, 9.0, 11.0, 13.0, 17.0, 19.0,
 			39.0, 34.0, 23.0, 15.0, 13.0, 12.0, 11.0, 11.0, 11.0, 11.0, 10.0, 9.0, 10.0, 11.0, 11.0, 13.0,
 			35.0, 32.0, 28.0, 24.0, 21.0, 16.0, 13.0, 12.0, 11.0, 11.0, 10.0, 10.0, 12.0, 12.0, 11.0, 10.0,
 			21.0, 20.0, 17.0, 15.0, 16.0, 18.0, 17.0, 13.0, 13.0, 12.0, 13.0, 12.0, 12.0, 14.0, 14.0, 13.0,
 			14.0, 14.0, 15.0, 14.0, 12.0, 14.0, 13.0, 12.0, 12.0, 12.0, 12.0, 13.0, 12.0, 14.0, 15.0, 13.0
 		};
 
 		SparseRealMatrix decimationOperator = decimationStrategy.getDecimationOperator(rangeVector.length, domainVector.length);
 		
 		RealMatrix range = new Array2DRowRealMatrix(rangeVector.length, 1);
 		range.setColumn(0, rangeVector);
 		
 		RealMatrix domain = new Array2DRowRealMatrix(domainVector.length, 1);
 		domain.setColumn(0, domainVector);
 
 		RealMatrix decimatedDomain = decimationOperator.multiply(domain);
 		
 		AffineTransform transform = affineKernel.encode(new SignalBlock(0, decimatedDomain), new SignalBlock(0, range));
 		
 		assertEquals(1.303533, transform.getDistance(), TestUtils.DELTA);
 		assertEquals(0.107998, transform.getScale(), TestUtils.DELTA);
 		assertEquals(84.911498, transform.getOffset(), TestUtils.DELTA);
 	}
 }
