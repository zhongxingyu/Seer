 package au.yibing;
 
 import java.io.*;
 import java.text.DecimalFormat;
 
 public class PlainMaxProfitFinder {
 
     public static void main(String[] args) throws IOException{
         String inputFilePath = "price_moves.csv";
         String outputFilePath = "result.txt";
 
         if (args.length > 0) {
             inputFilePath = args[0];
         }
 
         if (args.length > 1) {
             outputFilePath = args[1];
         }
 
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath));
 
 
         double lowPointPrice = 1;
         String lowPointDate = bufferedReader.readLine(); //set low point according to first line
         String sellPointDate = null;
         double buyPointPrice = -1;
         String buyPointDate = null;
         double currentPrice = 1;
         double maxProfit = Double.MIN_VALUE;
         String line;
         while ((line = bufferedReader.readLine()) != null) {
             String[] fields = line.split(",");
             double move = Double.valueOf(fields[0]);
             String date = fields[1];
             currentPrice = currentPrice * (move / 100 + 1);
             double curPossibleMaxProfit = currentPrice - lowPointPrice;
             if (curPossibleMaxProfit > maxProfit) {
                 maxProfit = curPossibleMaxProfit;
                 sellPointDate = date;
                 buyPointPrice = lowPointPrice;
                 buyPointDate = lowPointDate;
             }
             if (currentPrice < lowPointPrice) {
                 lowPointPrice = currentPrice;
                 lowPointDate = date;
             }
         }
 
         if (buyPointDate != null & sellPointDate != null) {
            writeResult(buyPointPrice, buyPointDate, sellPointDate, maxProfit, outputFilePath);
         }else {
             System.err.println("Cannot find any profit");
         }
     }
 
     private static void writeResult(double buyPointPrice,
                                     String buyPointDate,
                                     String sellPointDate,
                                     double profit,
                                     String outputFilePath) throws FileNotFoundException {
         PrintWriter printWriter = new PrintWriter(new FileOutputStream(outputFilePath));
         printWriter.println(buyPointDate);
         printWriter.println(sellPointDate);
         double profitRatio = profit / buyPointPrice * 100;
         DecimalFormat df = new DecimalFormat("#.###");
         printWriter.println(df.format(profitRatio));
         printWriter.flush();
         printWriter.close();
     }
 }
