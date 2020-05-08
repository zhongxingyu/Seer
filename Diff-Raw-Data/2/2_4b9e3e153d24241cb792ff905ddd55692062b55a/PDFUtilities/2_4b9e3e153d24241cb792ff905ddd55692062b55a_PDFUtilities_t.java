 package model.util;
 
 import com.lowagie.text.*;
 import java.awt.Color;
 import java.util.HashMap;
 
 /**
  *
  * @author skuarch
  */
 public class PDFUtilities {
 
     //==========================================================================
     public static Table tableHashMap(HashMap hm) throws Exception {
 
         if (hm == null) {
             throw new NullPointerException("hm is null");
         }
 
 
         Table table = null;
         Cell c1 = null;
         Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.BOLD, new Color(0, 0, 0));
         Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, new Color(0, 0, 0));
 
         try {
 
             table = new Table(2, 3);
             table.setPadding(2);
             table.setSpacing(2);
             //table.setSpaceBetweenCells(0);
             c1 = new Cell(new Paragraph("Propertie", headerFont));
             c1.setHeader(true);
             table.addCell(c1);
             c1 = new Cell(new Paragraph("Value", headerFont));
             table.addCell(c1);
             table.endHeaders();
 
             table.addCell(new Paragraph("collector", cellFont));
             table.addCell(new Paragraph(hm.get("collector").toString(), cellFont));
             table.addCell(new Paragraph("job", cellFont));
             table.addCell(new Paragraph(hm.get("job").toString(), cellFont));
 
             if (!hm.get("dates").toString().equalsIgnoreCase("not applicable")) {
                 table.addCell(new Paragraph("dates", cellFont));
                 table.addCell(new Paragraph(hm.get("dates").toString(), cellFont));
             }
 
             if (!hm.get("ipAddress").toString().equalsIgnoreCase("not applicable")) {
                 table.addCell(new Paragraph("ip address", cellFont));
                 table.addCell(new Paragraph(hm.get("ipAddress").toString(), cellFont));
             }
 
             if (!hm.get("drillDown").toString().equalsIgnoreCase("not applicable")) {
                 table.addCell(new Paragraph("drill down", cellFont));
                table.addCell(new Paragraph(hm.get("drillDown").toString(), cellFont));
             }
 
             if (!hm.get("ipProtocol").toString().equalsIgnoreCase("not applicable")) {
                 table.addCell(new Paragraph("ip protocols", cellFont));
                 table.addCell(new Paragraph(hm.get("ipProtocol").toString(), cellFont));
             }
 
             if (!hm.get("networkProtocol").toString().equalsIgnoreCase("not applicable")) {
                 table.addCell(new Paragraph("network protocols", cellFont));
                 table.addCell(new Paragraph(hm.get("networkProtocol").toString(), cellFont));
             }
 
             if (!hm.get("tcpProtocol").toString().equalsIgnoreCase("not applicable")) {
                 table.addCell(new Paragraph("tcp protocols", cellFont));
                 table.addCell(new Paragraph(hm.get("tcpProtocol").toString(), cellFont));
             }
 
             if (!hm.get("udpProtocol").toString().equalsIgnoreCase("not applicable")) {
                 table.addCell(new Paragraph("udp protocols", cellFont));
                 table.addCell(new Paragraph(hm.get("udpProtocol").toString(), cellFont));
             }
 
             if (!hm.get("typeService").toString().equalsIgnoreCase("not applicable")) {
                 table.addCell(new Paragraph("type service", cellFont));
                 table.addCell(new Paragraph(hm.get("typeService").toString(), cellFont));
             }
 
             if (!hm.get("websites").toString().equalsIgnoreCase("not applicable")) {
                 table.addCell(new Paragraph("websites", cellFont));
                 table.addCell(new Paragraph(hm.get("websites").toString(), cellFont));
             }
 
             if (!hm.get("hostname").toString().equalsIgnoreCase("not applicable")) {
                 table.addCell(new Paragraph("hostname", cellFont));
                 table.addCell(new Paragraph(hm.get("hostname").toString(), cellFont));
             }
 
         } catch (Exception e) {
             throw e;
         }
 
         return table;
 
     } // end tableSubPiece
 }
