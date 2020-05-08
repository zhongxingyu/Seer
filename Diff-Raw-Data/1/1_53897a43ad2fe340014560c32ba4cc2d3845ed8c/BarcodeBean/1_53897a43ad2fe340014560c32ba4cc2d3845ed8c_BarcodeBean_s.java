 package com.ocpsoft.pretty.examples.barcode.beans;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import javax.servlet.http.HttpServletResponse;
 
 import org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5Bean;
 import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
 
 import com.ocpsoft.pretty.faces.annotation.URLAction;
 import com.ocpsoft.pretty.faces.annotation.URLMapping;
 
 @Named
 @RequestScoped
 @URLMapping(id = "barcode", viewId = "/empty.jsf",
             pattern = "/barcode/#{ /[0-9]+/ barcodeBean.value }.png")
 public class BarcodeBean
 {
    private String value;
 
    @PostConstruct
    public void init()
    {
       System.out.println("Created instance of BarcodeBean");
    }
 
    @URLAction
    public void render() throws IOException
    {

       // get HttpServletResponse
       FacesContext context = FacesContext.getCurrentInstance();
       HttpServletResponse response =
                (HttpServletResponse) context.getExternalContext().getResponse();
 
       // set correct content type
       response.setContentType("image/png");
 
       // setup CanvasProvider to render bitmap to response stream
       BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                                                    response.getOutputStream(), "image/png", 150,
                                                    BufferedImage.TYPE_BYTE_BINARY, false, 0);
 
       // render barcode
       Interleaved2Of5Bean barcode = new Interleaved2Of5Bean();
       barcode.generateBarcode(canvas, value);
       canvas.finish();
 
       // mark response as completed
       context.responseComplete();
    }
 
    public String getValue()
    {
       return value;
    }
 
    public void setValue(final String value)
    {
       this.value = value;
    }
 }
