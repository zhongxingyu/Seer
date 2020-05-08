 package me.sheimi.pig.eval.demo;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import org.apache.hadoop.io.Text;
 import org.apache.pig.EvalFunc;
 import org.apache.pig.backend.executionengine.ExecException;
 import org.apache.pig.data.DataByteArray;
 import org.apache.pig.data.Tuple;
 import org.apache.pig.data.TupleFactory;
 
 public class ToBMP extends EvalFunc<DataByteArray> {
 	
 	@Override
 	public DataByteArray exec(Tuple input) {
 		// TODO Auto-generated method stub
 		if (input == null || input.size() == 0)
 			return null;
 		
 			byte[] bytesin;
 			try {
 				bytesin = ((DataByteArray) input.get(0)).get();
 				ByteArrayInputStream bais = new ByteArrayInputStream(bytesin); 
 				BufferedImage img = ImageIO.read(bais);
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				ImageIO.write(img, "bmp", baos);
 				byte[] byteout = baos.toByteArray();
 				DataByteArray dba = new DataByteArray(byteout);
 				return dba;
 			} catch (ExecException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
      }
 		return null;
 	}
 
 }
