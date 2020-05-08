 package org.lawrencebower.docgen.core.generator.utils;
 
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.Element;
 import com.lowagie.text.Phrase;
 import com.lowagie.text.Rectangle;
 import com.lowagie.text.pdf.PdfPCell;
 import com.lowagie.text.pdf.PdfPTable;
 import org.lawrencebower.docgen.core.document.component.DocComponent;
 import org.lawrencebower.docgen.core.document.component.DocComponentType;
 import org.lawrencebower.docgen.core.document.component.position.DocPosition;
 import org.lawrencebower.docgen.core.document.component.position.HorizontalAlignment;
 import org.lawrencebower.docgen.core.document.component.position.VerticalAlignment;
 import org.lawrencebower.docgen.core.document.component.table.TableCell;
 import org.lawrencebower.docgen.core.document.component.table.TableComponent;
 import org.lawrencebower.docgen.core.exception.DocGenException;
 import org.lawrencebower.docgen.core.generator.custom.renderer.CustomComponentRenderer;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class ITextTableGenerator {
 
     @Autowired
     private PDFGenUtils pdfUtils;
 
     @Autowired
     private CustomComponentRenderer componentRenderer;
 
     public PdfPTable generateTable(TableComponent tableComponent) {
 
         PdfPTable iTextTable = makeTable(tableComponent);
 
         setTableWidth(tableComponent, iTextTable);
 
         setColumnWidths(tableComponent, iTextTable);
 
         mapCells(tableComponent, iTextTable);
 
         mapTableAlignment(tableComponent, iTextTable);
 
         return iTextTable;
     }
 
     private void mapTableAlignment(TableComponent tableComponent, PdfPTable iTextTable) {
         HorizontalAlignment alignment = tableComponent.getPosition().getHorizontalAlignment();
         int iTextAlignment = HorizontalAlignment.mapToITextAlignment(alignment);
         iTextTable.setHorizontalAlignment(iTextAlignment);
     }
 
     private PdfPTable makeTable(TableComponent tableComponent) {
         int columnCount = tableComponent.getColumnCount();
         return new PdfPTable(columnCount);
     }
 
     private void setColumnWidths(TableComponent tableComponent, PdfPTable iTextTable) {
 
         int[] relativeWidths = tableComponent.getColumnWidths();
 
         if (relativeWidths.length == 0) {
             return;
         }
 
         try {
             iTextTable.setWidths(relativeWidths);
         } catch (DocumentException e) {
             throw new DocGenException(e);
         }
     }
 
     private void setTableWidth(TableComponent tableComponent, PdfPTable iTextTable) {
         float widthPercentage = tableComponent.getWithPercentage();
         if (widthPercentage != 0) {
             iTextTable.setWidthPercentage(widthPercentage);
         }
     }
 
     private void mapCells(TableComponent tableComponent, PdfPTable iTextTable) {
 
         for (TableCell tableCell : tableComponent.getAllRenderableCells()) {
 
             PdfPCell iTextCell = processCell(tableCell);
 
             mapCellAlignment(tableCell, iTextCell);
 
             mapCellSpans(tableCell, iTextCell);
 
             setCellColor(tableCell, iTextCell);
 
             setCellPadding(tableComponent, tableCell, iTextCell);
 
             setCellBorder(tableComponent.isRenderBorder(), iTextCell);
 
             iTextTable.addCell(iTextCell);
         }
     }
 
     private void mapCellSpans(TableCell tableCell, PdfPCell iTextCell) {
         iTextCell.setRowspan(tableCell.getRowSpan());
         iTextCell.setColspan(tableCell.getColSpan());
     }
 
     private void setCellBorder(boolean renderBorder,
                                PdfPCell iTextCell) {
 
         if (renderBorder) {
             iTextCell.setBorder(Rectangle.LEFT +
                                 Rectangle.RIGHT +
                                 Rectangle.TOP +
                                 Rectangle.BOTTOM);
         } else {
             iTextCell.setBorder(0);
         }
     }
 
     private void setCellPadding(TableComponent tableComponent,
                                 TableCell tableCell,
                                 PdfPCell iTextCell) {
 
         float padding = tableComponent.getTablePadding();
 
         /**
          * cell padding over rides table padding
          */
        if(tableCell.getPadding() != 0){
             padding = tableCell.getPadding();
         }
 
         iTextCell.setPadding(padding);
     }
 
     private void setCellColor(TableCell tableCell, PdfPCell iTextCell) {
         if (tableCell.hasBackgroundColor()) {
             iTextCell.setBackgroundColor(tableCell.getBackgroundColor());
         }
     }
 
     private void mapCellAlignment(TableCell tableCell, PdfPCell iTextCell) {
 
         mapVerticalAlignment(tableCell, iTextCell);
 
         mapHorizontalAlignment(tableCell, iTextCell);
     }
 
     private void mapVerticalAlignment(TableCell tableCell, PdfPCell iTextCell) {
 
         VerticalAlignment verticalAlignment = tableCell.getVerticalAlignment();
         int iTextVerticalAlignment = VerticalAlignment.mapToITextAlignment(verticalAlignment);
         iTextCell.setVerticalAlignment(iTextVerticalAlignment);
     }
 
     /**
      * sets the alignment on the ITextTable cell. The horizontal alignment will be ignored for all nested table
      * components except TableTextComponent. Components other than TableTextComponent should have their
      * HorizontalAlignment set.
      */
     private void mapHorizontalAlignment(TableCell tableCell, PdfPCell iTextCell) {
 
         DocComponent component = tableCell.getComponent();
         DocPosition componentPosition = component.getPosition();
         HorizontalAlignment horizontalAlignment = componentPosition.getHorizontalAlignment();
 
         int iTextHorizontalAlignment = HorizontalAlignment.mapToITextAlignment(horizontalAlignment);
         iTextCell.setHorizontalAlignment(iTextHorizontalAlignment);
     }
 
     /**
      * For TableText to be dealt with, the constructor arg needs to be used, otherwise all other element types
      * use the addElement() method
      */
     private PdfPCell processCell(TableCell tableCell) {
 
         DocComponent component = tableCell.getComponent();
 
         Element iTextElement = componentRenderer.createComponent(component);
 
         PdfPCell iTextCell;
 
         if(component.getComponentType() == DocComponentType.TABLE_TEXT){
             iTextCell = new PdfPCell((Phrase) iTextElement);
         }else{
             iTextCell = new PdfPCell();
             iTextCell.addElement(iTextElement);
         }
 
         return iTextCell;
     }
 }
