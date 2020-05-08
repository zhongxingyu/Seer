 package invenio.pdf.core;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * Handling parameters of the plots extractor - they are stored in an external
  * configuration file and this class is responsible of providing access to them.
  *
  * Properties object follow the singleton pattern
  * @author piotr
  */
 public class ExtractorParameters extends Properties {
 
     private static ExtractorParameters properties = null;
 
     private ExtractorParameters() {
         FileInputStream fis = null;
         this.resetToDefaultValues();
 
         try {
             fis = new FileInputStream(new File(ExtractorParameters.confFilePath));
             this.load(fis);
             fis.close();
         } catch (Exception ex) {
             // oops... we need to read default values
             this.resetToDefaultValues();
         }
     }
 
     /**
      * Getting an instance of the project properties
      * 
      * @return
      */
     public static ExtractorParameters getExtractorParameters() {
         if (properties == null) {
             // TODO : read this information from some configuration XML file
             properties = new ExtractorParameters();
         }
         return properties;
     }
     private static String confFilePath;
 
     /**
      * Registers a path of the configuration file. 
      * This name will be always used to obtain the configuration.
      * @param fname 
      */
     public static void registerConfigurationFile(String fname) {
         ExtractorParameters.confFilePath = fname;
     }
 
     /**
      * Save parameters of the extractor into a file
      * 
      * @param fname Path of a file to save the parameters
      */
     public void saveExtractorParameters(String fname) throws FileNotFoundException, IOException {
         FileOutputStream fos = new FileOutputStream(new File(fname));
         this.store(fos, "This is a configuration file of the plots extractor");
     }
 
     /***** accessing the properties */
     /**
      * Return the comment that will be included in the configuration file - description of possible parameters
      * @return 
      */
     public static String getComment() {
         return "This is a configuration file for the PDF figures extractor Possible parameters (with types) are :"
                 + "minimal_aspect_ratio"
                 //    + "maximal_aspect_ratio"
                 + "vertical_graphical_margin : a fraction of the page height that will be considered a margin of a graphical operation"
                 + "horizontal_graphical_margin : A fraction of the page width that will be considered a margin of a graphical operation"
                 + "vertical_text_margin : Returns a fraction of the page height that will be considered a vertical margin for all text operations being considered part of a plot"
                 + "minimal_figure_operations_number : Minimal number of operations that a figure has to contain. If inside a figure candidate there are rendered images refeences, this value is ignored"
                 + ""
                 + ""
                 + "minimal_figure_width : minimal width (as fraction of page width) that a figure has to have"
                 + "minimal_figure_height : minimal height (as fraction of page height) that a figure has to have"
                 + "minimal_column_width : minimal width of a detected page column. (smaller columns will be clustered with others)"
                 + "minimal_graphical_area_fraction : fraction of an area of a figure candidate allowing it to be considered figure"
                 + "minimal_figure_graphical_operations_number: minimal number of graphical operations that have to be included in a figure (unless external graphics)"
                 + "minimal_figure_graphical_operations_fraction: Minimal fraction of graphical operations inside of a figure (unless inline or external graphics)" 
                 + "maximal_inclusion_height - maximal percantage of page height that can be included in a figure in the case, caption can not be matched with figure candidate";
 
         // + "minimal_column_height : minimal height of a detected page column. (smaller columns will be clustered with others)";
     }
 
     /**
      * Sets default values for all possible arguments
      */
     public final void resetToDefaultValues() {
 
         /**
          * Returns the maximal aspect ratio which is a double value
          * @return
          */
         this.setProperty("minimal_aspect_ratio", "0.1");
         //this.setProperty("maximal_aspect_ratio", "10");
         this.setProperty("vertical_graphical_margin", "0.02");
         this.setProperty("horizontal_graphical_margin", "0.1");
         this.setProperty("vertical_text_margin", "0.005");
         this.setProperty("horizontal_text_margin", "0.05");
         this.setProperty("vertical_plot_text_margin", "0.00065");
         this.setProperty("horizontal_plot_text_margin", "0.01");
         this.setProperty("page_scale", "2");
         this.setProperty("minimal_margin_width", "0.3");
        //this.setProperty("minimal_margin_width", "0");
         this.setProperty("horizontal_emptiness_radius", "0.01");
         this.setProperty("vertical_emptiness_radius", "0.005");
         this.setProperty("maximum_non_breaking_fraction", "0.005");
         this.setProperty("colour_emptiness_threshold", "10");
         this.setProperty("empty_pixel_colour_r", "255");
         this.setProperty("empty_pixel_colour_g", "255");
         this.setProperty("empty_pixel_colour_b", "255");
 
         this.setProperty("minimal_vertical_separator_height", "0.4");
         this.setProperty("minimal_column_width", "0.25"); // minimally 1/4 of the page for a column
         this.setProperty("minimal_figure_operations_number", "10"); // miniminal number of operations inside of a figure ... unless external graphics
         this.setProperty("minimal_figure_graphical_operations_number", "4");
         this.setProperty("minimal_figure_graphical_operations_fraction", "0.10");
         this.setProperty("produce_output", "true");
 
         //this.setProperty("minimal_column_height", "0.25"); // minimally 1/4 of the page for a column
 
         this.setProperty("generate_debug_information", "true");
         //his.setProperty("generate_debug_information", "false");
         this.setProperty("generate_plot_provenance", "true");
         this.setProperty("generate_svg", "true");
 
 
         this.setProperty("minimal_figure_height", "0.1"); // minimum 10% of the page
         this.setProperty("minimal_figure_width", "0.15"); // minimum 25% of the page
 
         this.setProperty("minimal_graphical_area_fraction", "0.15");
         this.setProperty("maximal_inclusion_height", "0.10");
         
     }
 
     public double getMinimalGraphicalAreaFraction() {
         return Double.parseDouble(this.getProperty("minimal_graphical_area_fraction"));
     }
 
     public double getMinimalPageLayoutColumnWidth() {
         return Double.parseDouble(this.getProperty("minimal_column_width"));
     }
 
     // public double getMinimalPageLayoutColumnHeight() {
     //     return Double.parseDouble(this.getProperty("minimal_column_height"));
     // }
 
     /* return the maximal */
     public double getMinimalAspectRatio() {
         return Double.parseDouble(this.getProperty("minimal_aspect_ratio"));
     }
 
     /**
      * Returns the maximal aspect ratio which is a double value
      * @return
      */
     public double getMaximalAspectRatio() {
         return 1 / this.getMinimalAspectRatio();
     }
 
     /**
      * Returns a fraction of the page height that will be considered a margin of
      * a graphical operation
      * @return
      */
     public double getVerticalGraphicalMargin() {
         return Double.parseDouble(this.getProperty("vertical_graphical_margin"));
         // return 0.02;
         //return 0.003;
     }
 
     /**
      * Returns a fraction of the page height that will be considered
      * a horizontal margin of graphical operations
      * @return
      */
     public double getHorizontalGraphicalMargin() {
         //return 0.003;
         //return 0.10;
         return Double.parseDouble(this.getProperty("horizontal_graphical_margin"));
     }
 
     /**
      * Returns a fraction of the page height that will be considered a vertical
      * margin for all text operations being considered part of a plot
      *
      * @return
      */
     public double getVerticalTextMargin() {
         return Double.parseDouble(this.getProperty("vertical_text_margin"));
         //return 0.005;
         //return 0.007;
     }
 
     /**
      * Returns a fraction of the page height that will be considered a vertical
      * margin for all text operations
      * 
      * @return
      */
     public double getVerticalPlotTextMargin() {
         return Double.parseDouble(this.getProperty("vertical_plot_text_margin"));
         //return 0.00065;
         //return 0.003;
     }
 
     /**
      * Returns a fraction of the page width that will be considered the horizontal
      * margin of all text operations
      * @return
      */
     public double getHorizontalPlotTextMargin() {
 //        return 0.01;
         return Double.parseDouble(this.getProperty("horizontal_plot_text_margin"));
     }
 
     /**
      * Returns a fraction of the page width that will be considered the horizontal
      * margin of all text operations
      * @return
      */
     public double getHorizontalTextMargin() {
         return Double.parseDouble(this.getProperty("horizontal_text_margin"));
         //return 0.05;
         //return 0.05; // this value is much bigger that the vertical margin
         // as it is unlikely that the text block will be lying next
         // to another text block (they are rather located below each other)
     }
 
     /**
      * Return the coefficient by which the page will be scaled
      * @return
      */
     public int getPageScale() {
         //return 2;
         return Integer.parseInt(this.getProperty("page_scale"));
     }
 
     ///// Parameters connected with detection of the page layout
     /**
      * Returns the minimal fraction of the page width that will not be broken into
      * @return
      */
     public double getMinimalMarginWidth() {
 //        return 0.3;
         return Double.parseDouble(this.getProperty("minimal_margin_width"));
     }
 
     /**
      * When we check if a point is empty, the pixel must be empty together with
      * its surrounding of some radius.
      *
      * This function returns the fraction of the page width that is considered
      * to be this radius.
      * 
      * @return
      */
     public double getHorizontalEmptinessRadius() {
         //return 0.01;
         return Double.parseDouble(this.getProperty("horizontal_emptiness_radius"));
     }
 
     /**
      * Used to determine the minimal empty surrounding of a horizontal separator
      *
      * @return
      */
     public double getVerticalEmptinessRadius() {
 //        return 0.005;
         return Double.parseDouble(this.getProperty("vertical_emptiness_radius"));
     }
 
     /**
      * Returns the fraction of the page height that can be covered with
      * non-empty pixels and not break a separator.
      *
      * @return
      */
     public double getMaximalNonBreakingFraction() {
 //        return 0.005;
         return Double.parseDouble(this.getProperty("maximum_non_breaking_fraction"));
     }
 
     public int getColorEmptinessThreshold() {
 //        return 10;
         return Integer.parseInt(this.getProperty("colour_emptiness_threshold"));
     }
 
     /**
      * Returns the color that can be considered an empty space inside an image
      * @return
      */
     public int[] getEmptyPixelColor() {
         int[] res = {Integer.parseInt(this.getProperty("empty_pixel_colour_r")),
             Integer.parseInt(this.getProperty("empty_pixel_colour_g")),
             Integer.parseInt(this.getProperty("empty_pixel_colour_b"))};
         return res;
     }
 
     /**
      * The percentage of hte page height that is considered to be the minimal
      * separator
      *
      * @return
      */
     public double getMinimalVerticalSeparatorHeight() {
 //        return 0.4;
         return Double.parseDouble(this.getProperty("minimal_vertical_separator_height"));
     }
 
     public boolean generateDebugInformation() {
         //return false;
         return Boolean.parseBoolean(this.getProperty("generate_debug_information"));
     }
 
     public boolean generatePlotProvenance() {
 //        return false;
         return Boolean.parseBoolean(this.getProperty("generate_plot_provenance"));
     }
 
     public boolean generateSVG() {
 //        return false;
         return Boolean.parseBoolean(this.getProperty("generate_svg"));
     }
 
     public int getMinimalFiguresGraphicalOperationsNumber() {
         return Integer.parseInt(this.getProperty("minimal_figure_graphical_operations_number"));
     }
     
     public double getMinimalFiguresGraphicalOperationsFraction() {
         return Double.parseDouble(this.getProperty("minimal_figure_graphical_operations_fraction"));
     }
 
     public int getMinimalFiguresOperationsNumber() {
         return Integer.parseInt(this.getProperty("minimal_figure_operations_number"));
     }
 
     public double getMinimalFigureWidth() {
         return Double.parseDouble(this.getProperty("minimal_figure_width"));
     }
 
     public double getMinimalFigureHeight() {
         return Double.parseDouble(this.getProperty("minimal_figure_height"));
     }
 
     /**
      * the minimal percentage of page height that can be included in misdetected figure
      * in the case, caption can not be matched
      * @return 
      */
     public double getMaximalInclusionHeight() {
         return Double.parseDouble(this.getProperty("maximal_inclusion_height"));
     }
     
     /**
      * Determines if any output files should be written (useful for testing jjust the number of extractions
      * @return 
      */
     public boolean shouldProduceOutput(){
         return Boolean.parseBoolean(this.getProperty("produce_output"));
 
     }
 }
