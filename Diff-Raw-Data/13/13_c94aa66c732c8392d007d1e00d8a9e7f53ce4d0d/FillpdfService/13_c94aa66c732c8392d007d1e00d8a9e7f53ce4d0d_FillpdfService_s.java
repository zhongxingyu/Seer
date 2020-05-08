 package com.ocdevel;
 
 import com.itextpdf.text.BadElementException;
 import java.util.Set;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.io.FileOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import com.itextpdf.text.pdf.AcroFields;
 import com.itextpdf.text.pdf.PdfReader;
 import com.itextpdf.text.pdf.XfdfReader;
 import com.itextpdf.text.pdf.PdfStamper;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.codec.Base64;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.pdf.PdfContentByte;
 
 /**
  *
  * @author renelle
  * @author wizonesolutions
  */
 
 /**
  *  PDF::Stamper provides an interface into iText's PdfStamper allowing for the
  *  editing of existing PDF's as templates. PDF::Stamper is not a PDF generator,
  *  it allows you to edit existing PDF's and use them as templates.
  *
  *  == Creation of templates
  *
  *  Templates currently can only be created using Adobe LiveCycle
  *  Designer which comes with the lastest versions of Adobe Acrobat
  *  Professional.  Using LiveCycle Designer you can create a form and
  *  add textfield's for text and button's for images.
  *
  *  == Example
  *
  *  pdf = PDF::Stamper.new("my_template.pdf")
  *  pdf.text :first_name, "Jason"
  *  pdf.text :last_name, "Yates"
  *  pdf.image :photo, "photo.jpg"
  *  pdf.save_as "my_output"
  */
 public class FillpdfService{
   ByteArrayOutputStream baos  = null;
   PdfStamper stamp            = null;
   AcroFields form             = null;
 
 //  public static void main(String [ ] args){
 //
 //    FillpdfService svc = new FillpdfService("/Users/lefnire/test_template.pdf", "file");
 //    //svc.text("Date of Birth", "test");
 //    svc.text("form1[0].#subform[0].text_field01[0]", "test");
 ////    System.out.println(svc.toString());
 //    svc.saveAs("/Users/lefnire/Downloads/test.pdf");
 //  }
 
 
   public FillpdfService(String pdf, String type)
     throws IOException, DocumentException{
     PdfReader reader = null;
 
     /*NOTE I'd rather use a ByteArrayOutputStream.  However I
       couldn't get it working.  Patches welcome. */
     //@tmp_path = File.join(Dir::tmpdir,  'pdf-stamper-' + rand(10000).to_s + '.pdf')
     
     if(type.equals("file")){
       reader = new PdfReader(pdf);
     }else{
       reader = new PdfReader(Base64.decode(pdf));
     }
     this.baos = new ByteArrayOutputStream();
     this.stamp = new PdfStamper(reader, this.baos); //FileOutputStream.new(@tmp_path))
     this.form = this.stamp.getAcroFields();
     
   }
 
 
     /**
      * Takes the PDF output and sends as a string.  Basically it's sole
      * purpose is to be used with send_data in rails.
      */
     public String toByteArray()
       throws IOException, DocumentException{
       this.fill();
       return Base64.encodeBytes(baos.toByteArray());
     }
 
    public String toByteArray(boolean flatten) {
      this.stamp.setFormFlattening(flatten);
      return Base64.encodeBytes(baos.toByteArray());
     }
     
     /**
      * Set a textfield defined by key and text to value.
      */
     public void text(String key, String value)
       throws IOException, DocumentException{
       this.form.setField(key, value); // Value must be a string or itext will error.
     }
 
     // Saves the PDF into a file defined by path given.
     public void saveAs(String file)
       throws IOException, DocumentException{
       this.fill();
       FileOutputStream fout = new FileOutputStream(file);
       this.baos.writeTo(fout);
       fout.close();
     }
 
     private void fill()
       throws IOException, DocumentException{
       this.stamp.setFormFlattening(true);
       this.stamp.close();
       this.baos.flush();
     }
 
 
     public ArrayList<HashMap> parse(){
         ArrayList<HashMap> arr = new ArrayList();
         // Loop over the fields and get info about them
         Set<String> fields = this.form.getFields().keySet();
         for (String key : fields) {
             String type = null;
             switch (this.form.getFieldType(key)) {
             case AcroFields.FIELD_TYPE_CHECKBOX:
                 type = "Checkbox";
                 break;
             case AcroFields.FIELD_TYPE_COMBO:
                 type = "Combobox";
                 break;
             case AcroFields.FIELD_TYPE_LIST:
                 type = "List";
                 break;
             case AcroFields.FIELD_TYPE_NONE:
                 type = "None";
                 break;
             case AcroFields.FIELD_TYPE_PUSHBUTTON:
                 type = "Pushbutton";
                 break;
             case AcroFields.FIELD_TYPE_RADIOBUTTON:
                 type = "Radiobutton";
                 break;
             case AcroFields.FIELD_TYPE_SIGNATURE:
                 type = "Signature";
                 break;
             case AcroFields.FIELD_TYPE_TEXT:
                 type = "Text";
                 break;
             default:
                 type = "?";
             }
             HashMap map = new HashMap();
             map.put("name", key);
             map.put("type", type);
             map.put("value", this.form.getField(key));
             arr.add(map);
         }
         return arr;
     }
 
     public String parse_as_xfdf(){
       String xml = "<fields>";
       ArrayList<HashMap> arr = this.parse();
       for (HashMap map : arr) {
         //xml += "\n<field name='#{key}' type='#{form.getFieldType(key)}' value='#{form.getField(key)}'/>"
         xml += "\n<field name='" + map.get("name") + "' type='" + map.get("type") + "' value='" + map.get("value") + "'/>";
       }
       xml += "\n</fields>";
       return xml;
     }
 
     public void merge(String xfdf, String type)
       throws IOException, DocumentException {
       XfdfReader xfdfreader = null;
       if(type.equals("file")){
         xfdfreader = new XfdfReader(xfdf);
       }else{
         xfdfreader = new XfdfReader(xfdf.getBytes());
       }
       this.form.setFields(xfdfreader);
     }
 
     /**
      * Set a button field defined by key and replaces with an image.
      */
     public void image(String key, String image_path, String type)
       throws BadElementException, MalformedURLException, IOException, DocumentException{
       // Idea from here http://itext.ugent.be/library/question.php?id=31
       // Thanks Bruno for letting me know about it.
       Image img = null;
       if(type.equals("file")){
         img = Image.getInstance(image_path);
       }else{
         img = Image.getInstance(image_path.getBytes());
       }
       float[] img_field = this.form.getFieldPositions(key);
 
       Rectangle rect = new Rectangle(img_field[1], img_field[2], img_field[3], img_field[4]);
       img.scaleToFit(rect.getWidth(), rect.getHeight());
       img.setAbsolutePosition(
         (img_field[1] + (rect.getWidth() - img.getScaledWidth()) / 2),
         (img_field[2] + (rect.getHeight() - img.getScaledHeight()) /2)
       );
 
       PdfContentByte cb = this.stamp.getOverContent((int)img_field[0]);
       cb.addImage(img);
     }
 
}
