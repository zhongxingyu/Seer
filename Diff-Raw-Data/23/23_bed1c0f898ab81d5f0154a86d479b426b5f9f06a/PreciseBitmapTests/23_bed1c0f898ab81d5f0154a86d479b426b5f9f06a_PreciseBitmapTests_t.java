 package com.cateye.core.native_;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 import com.cateye.core.native_.PreciseBitmap;
 
 public class PreciseBitmapTests {
 	@Test
 	public void test_precisebitmap_big_leak()
 	{
 		for (int i = 0; i < 1000; i++)
 		{
 			PreciseBitmap pbmp = new PreciseBitmap();
 			PreciseBitmap.Init(pbmp, 3000, 3000);
 			
 			PreciseBitmap pbmp2 = new PreciseBitmap();
 			PreciseBitmap.Copy(pbmp, pbmp2);
 			PreciseBitmap.Free(pbmp);
 			PreciseBitmap.Free(pbmp2);
 		}
 	}
 	
 	@Test
 	public void test_precisebitmap_small_leak()
 	{
 		try
 		{
 			final int CYCLES = 10000; 
 			for (int i = 0; i < CYCLES; i++)
 			{
 				PreciseBitmap pbmp = new PreciseBitmap();
 				PreciseBitmap.Init(pbmp, 30, 30);
 				
 				PreciseBitmap pbmp2 = new PreciseBitmap();
 				PreciseBitmap.Copy(pbmp, pbmp2);
 				PreciseBitmap.Free(pbmp);
 				PreciseBitmap.Free(pbmp2);
 				
 				if (i % (CYCLES / 100) == 0)
 					System.out.println((i / (CYCLES / 100)) + "%");
 			}
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 	
	@Test(timeout=5000)
 	public void test_precisebitmap_outofmemory()
 	{
 		final int W = 300, H = 300;
 		ArrayList<PreciseBitmap> pbmps = new ArrayList<PreciseBitmap>(); 
 		boolean outofmem = false;
 		
 		do
 		{
 			PreciseBitmap pbmp = new PreciseBitmap();
 			
 			if (PreciseBitmap.Init(pbmp, W, H) == PreciseBitmap.BITMAP_RESULT_OUT_OF_MEMORY)
 				outofmem = true;
 			else
 				pbmps.add(pbmp);
 		}
 		while (!outofmem);
 		
 		System.out.printf("%1$d images %2$dx%3$d allocated before out of memory. Freeing", pbmps.size(), W, H);
 		
 		for (int i = 0; i < pbmps.size(); i++)
 			PreciseBitmap.Free(pbmps.get(i));
 	}
 }
