 package com.vnexit.fingerprint.datamodel;
 
 import static org.junit.Assert.*;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.WritableRaster;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class TestImageProcess {
 	BufferedImage mFinger;
 	Pixel[][] pi;
 	ImageProcess imgProcess;
 	WritableRaster wr;
 
 	@Test
 	public void testConvertImagetoGray() throws IOException {
 		mFinger = ImageIO.read(getClass().getResource(
				"../datatest/vantay2.png"));
 		imgProcess = new ImageProcess(mFinger);
 		wr = mFinger.getRaster();
 		pi = imgProcess.grayExchange();
 		Assert.assertEquals(pi.length, mFinger.getHeight());
 		for (int i = 0; i < mFinger.getWidth(); i++) {
 			for (int j = 0; j < mFinger.getHeight(); j++) {
 				Assert.assertEquals(pi[i][j].mBlue, pi[i][j].mGreen);
 			}
 		}
 	}
 
 }
