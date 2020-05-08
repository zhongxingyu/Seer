 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author ugurcan
  */
 package yazLabChartLib;
 import java.io.IOException;
 import java.util.*;
 import java.lang.Math;
 import java.sql.*;
 import java.awt.*;
 import java.awt.FontMetrics;
 import java.awt.Font;
 import java.awt.Graphics;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 public class yazLabChartMain extends JFrame{
 
     public enum ChartType
     {
         Bar,Pie;
     }
 
    public int topNCount = 1000;
     public Object dataSource;
     public ChartType chartType = ChartType.Pie;
     int cx=0,cy=0;
     int lx=0,ly=0;
     int strWidth=0;
     ArrayList dsArrayList = new ArrayList();
     ResultSet dsResultSet;
     String[] dsArray;
     String[] name = new String[topNCount+1];
     int[] values = new int[topNCount+1];
     String[] temp = new String[2];
     int i,j,k;
     int count = 0;
      int total = 0;
      int sum = 0;
 
     	public void export(JFrame f,String path) {
 
 		Dimension size = f.getSize();
 		BufferedImage image = new BufferedImage(f.getWidth(), f.getHeight(),
 				BufferedImage.TYPE_INT_RGB);
 		Graphics g = image.getGraphics();
 		f.printAll(g);
 		g.dispose();
 		try {
 			ImageIO.write(image, "jpg", new File(path));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		f.setVisible(false);
 
 	}
     public void DataBind(JFrame frame) throws SQLException, IOException
     {
 
         if (dataSource instanceof ResultSet)
         {
             try {
                 dsResultSet = (ResultSet)dataSource;
                 sum=0;
                 i=0;
                 while(dsResultSet.next()) {
                     temp[0] = dsResultSet.getString("Name");
                     j = dsResultSet.getInt("Value");
                     if (i < topNCount){
                     name[i] = temp[0];
                     values[i] = j;
                     total += j;
                 }
                     else{
                         name[topNCount] = "Diger";
                         sum += j;
                         total+=j;
 
                 }
                     i++;
                 }
                 values[topNCount] = sum;
                 count = i;
             }
             catch(SQLException err)
             {
                 System.out.println(err.getMessage());
             }
 
         }
 
         if (dataSource instanceof ArrayList)
         {
             sum = 0;
             dsArrayList = (ArrayList)dataSource;
             for (i=0;i<dsArrayList.size();i++){
                 temp = dsArrayList.get(i).toString().split(",");
                 if (i < topNCount){
                     name[i] = temp[0];
                     values[i] = Integer.parseInt(temp[1]);
                     total += values[i];
                 }
                 else{
                     name[topNCount] = "Diger";
                     sum += Integer.parseInt(temp[1]);
                     total += Integer.parseInt(temp[1]);
 
                 }
 
             }
             values[topNCount] = sum;
             count = i;
         }
 
         if (dataSource.getClass().isArray())
         {
             sum=0;
             dsArray = (String[])dataSource;
             for (i=0;i<dsArray.length;i++)
             {
                 temp = dsArray[i].split(",");
                 if (i < topNCount){
                     name[i] = temp[0];
                     values[i] = Integer.parseInt(temp[1]);
                     total += values[i];
                 }
                 else{
                     name[topNCount] = "Diger";
                     sum += Integer.parseInt(temp[1]);
                     total += Integer.parseInt(temp[1]);
                 }
 
             }
             values[topNCount] = sum;
             count = i;
 
         }
 
         if (chartType == chartType.Bar)
         {
             double[] value = new double[topNCount];
             for (i=0;i<count;i++)
             {
                 value[i] = (double)values[i];
 
             }
             frame.setContentPane(new barChart(name, value,"",topNCount, count));
            
         }
         if (chartType == chartType.Pie)
         {
             frame.setContentPane(new pieChart(name, values, topNCount, count, total));
            
 
        }
 
             }
 
 }
