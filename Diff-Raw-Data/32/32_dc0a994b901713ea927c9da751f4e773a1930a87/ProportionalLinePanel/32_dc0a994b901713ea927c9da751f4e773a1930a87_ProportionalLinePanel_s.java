 package org.orbisgis.view.toc.actions.cui.legends.panels;
 
 import org.gdms.data.DataSource;
 import org.orbisgis.core.renderer.se.parameter.ParameterException;
 import org.orbisgis.legend.structure.fill.constant.ConstantSolidFill;
 import org.orbisgis.legend.structure.stroke.ProportionalStrokeLegend;
 import org.orbisgis.legend.thematic.proportional.ProportionalLine;
 import org.orbisgis.view.toc.actions.cui.components.CanvasSE;
 import org.orbisgis.view.toc.actions.cui.legends.components.*;
 import org.orbisgis.view.toc.actions.cui.legends.ui.PnlUniqueLineSE;
 import org.xnap.commons.i18n.I18n;
 import org.xnap.commons.i18n.I18nFactory;
 
 import javax.swing.*;
 
 /**
  * Settings panel for Proportional Line.
  *
  * @author Adam Gouge
  */
 public class ProportionalLinePanel extends AbsPanel {
 
     private static final I18n I18N = I18nFactory.getI18n(ProportionalLinePanel.class);
 
     private DataSource dataSource;
 
     private PLineFieldsComboBox pLineFieldsComboBox;
     private ColorLabel colorLabel;
     private LineUOMComboBox lineUOMComboBox;
     private MaxSizeSpinner maxSizeSpinner;
     private MinSizeSpinner minSizeSpinner;
 
     private LineOpacitySpinner lineOpacitySpinner;
     private DashArrayField dashArrayField;
 
     /**
      * Constructor
      *
      * @param legend     Legend
      * @param preview    Preview
      * @param dataSource DataSource
      */
     public ProportionalLinePanel(ProportionalLine legend,
                                  CanvasSE preview,
                                  DataSource dataSource) {
         super(legend, preview, I18N.tr(PnlUniqueLineSE.LINE_SETTINGS));
         this.dataSource = dataSource;
         init();
         addComponents();
     }
 
     @Override
     protected ProportionalLine getLegend() {
         return (ProportionalLine) legend;
     }
 
     @Override
     protected void init() {
         ProportionalStrokeLegend strokeLegend = getLegend().getStrokeLegend();
         ConstantSolidFill fillAnalysis = (ConstantSolidFill) strokeLegend.getFillAnalysis();
 
         pLineFieldsComboBox = PLineFieldsComboBox
                 .createInstance(dataSource, getLegend(), preview);
         colorLabel = new ColorLabel(fillAnalysis, preview);
         lineUOMComboBox = new LineUOMComboBox(getLegend(), preview);
         try {
             maxSizeSpinner = new MaxSizeSpinner(getLegend(), preview);
             minSizeSpinner = new MinSizeSpinner(getLegend(), maxSizeSpinner);
             maxSizeSpinner.setMinSizeSpinner(minSizeSpinner);
         } catch (ParameterException e) {
             e.printStackTrace();
         }
         lineOpacitySpinner = new LineOpacitySpinner(fillAnalysis, preview);
         dashArrayField = new DashArrayField(strokeLegend, preview);
     }
 
     @Override
     protected void addComponents() {
         // Field
        add(new JLabel(I18N.tr(FIELD)));
         add(pLineFieldsComboBox, COMBO_BOX_CONSTRAINTS);
         // Color
         add(new JLabel(I18N.tr("Color")));
         add(colorLabel);
         // Unit of Measure - line width
         add(new JLabel(I18N.tr(LINE_WIDTH_UNIT)));
         add(lineUOMComboBox, COMBO_BOX_CONSTRAINTS);
         // Max width
         add(new JLabel(I18N.tr("Max width")));
         add(maxSizeSpinner, "growx");
         // Min width
         add(new JLabel(I18N.tr("Min width")));
         add(minSizeSpinner, "growx");
         // Opacity
         add(new JLabel(I18N.tr(OPACITY)));
         add(lineOpacitySpinner, "growx");
         // Dash array
         add(new JLabel(I18N.tr(DASH_ARRAY)));
         add(dashArrayField, "growx");
     }
 }
