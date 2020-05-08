 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.wupp.client.alkis;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import sun.awt.resources.awt;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import java.awt.Toolkit;
 
 import javax.swing.JTextField;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.PlainDocument;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mroncoroni
  * @version  $Revision$, $Date$
  */
 public class ParcelInputField extends javax.swing.JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String PROP_CURRENT_PARCEL = "currentParcel";         // NOI18N
     public static final String PROP_DISTRICT_NUMBER = "districtNumber";       // NOI18N
     public static final String PROP_PARCEL_NUMBER = "parcelNumber";           // NOI18N
     public static final String PROP_PARCEL_NUMERATOR = "parcelNumerator";     // NOI18N
     public static final String PROP_PARCEL_DENOMINATOR = "parcelDenominator"; // NOI18N
     public static final String PROP_DISTRICT_NAME = "districtName";           // NOI18N
     public static final String PROP_VALID_PARCEL_NUMBER = "validParcelNr";    // NOI18N
 
     //~ Enums ------------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     public enum BlockType {
 
         //~ Enum constants -----------------------------------------------------
 
         DISTRICT, PARCEL, PARCEL_NUMERATOR, PARCEL_DENOMINATOR
     }
 
     //~ Instance fields --------------------------------------------------------
 
     // --- bindable
     private String currentParcel;
     private String districtNumber;
     private String parcelNumber;
     private String parcelNumerator;
     private String parcelDenominator;
     // ---
     // --- read-only bindable
     private String districtName;
     private boolean validParcelNr = false;
     // ---
     private final ParcelInputFieldConfig config;
     private boolean overwritten = false;
     private boolean changeFocus = true;
     private boolean writeOver = true;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel lblDelimitier1;
     private javax.swing.JLabel lblDelimitier2;
     private javax.swing.JLabel lblDelimitier3;
     private javax.swing.JTextField txtDistrict;
     private javax.swing.JTextField txtLandParcelDenominator;
     private javax.swing.JTextField txtLandParcelNumerator;
     private javax.swing.JTextField txtParcel;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Needed by the GUI-designer, not for usage.
      */
     public ParcelInputField() {
         this(ParcelInputFieldConfig.FallbackConfig);
     }
 
     /**
      * Creates new form Flurstueckseingabe.
      *
      * @param  config  DOCUMENT ME!
      */
     public ParcelInputField(final ParcelInputFieldConfig config) {
         this.config = config;
 
         initComponents();
 
         txtDistrict.setDocument(new PlainDocument() {
 
                 @Override
                 public void insertString(final int offs, String str, final AttributeSet a) throws BadLocationException {
                     txtDistrict.setForeground(Color.BLACK);
 
                     if (str == null) {
                         return;
                     }
                     // removes all characters except "-_%", digits, alpha charactes and german vowel mutuations
                     str = str.replaceAll("[^-\\u00F6\\u00D6\\u00FC\\u00DC\\u00E4\\u00C4\\u00dfa-zA-Z0-9_%]", "");
                     String newStr = str;
                     int maxLen = config.getMaxLenDistrictNumberField();
                     if ((txtDistrict.getDocument().getText(0, txtDistrict.getDocument().getLength()) + str).matches(
                                     "^05.*")) {
                         maxLen += 2;
                     }
                     if ((str.length() > 0)
                                 && ((str.length() > (maxLen - offs))
                                     || str.substring(0, Math.min(maxLen + 1 - offs, str.length())).contains(
                                         config.getDelimiter1AsString()))) {
                         int pos = maxLen - offs;
                         int posDel = str.indexOf(config.getDelimiter1());
                         if ((posDel >= 0) && (posDel < pos)) {
                             pos = posDel;
                         }
 
                         while (((str.indexOf('_') >= 0) && (str.indexOf('_') < pos))
                                     || ((str.indexOf('%') >= 0) && (str.indexOf('%') < pos))) {
                             str = str.replaceFirst("[_%]", "");
                             posDel = str.indexOf(config.getDelimiter1());
                             if ((posDel >= 0) && (posDel < pos)) {
                                 pos = posDel;
                             }
                         }
                         if (changeFocus) {
                             txtParcel.requestFocusInWindow();
                         }
 
                         newStr = str.substring(0, pos);
 
                         if (writeOver) {
                             String writeOverStr;
                             if (str.indexOf(config.getDelimiter1()) == pos) {
                                 writeOverStr = str.substring(pos + 1);
                             } else {
                                 writeOverStr = str.substring(pos);
                             }
                             if (!writeOverStr.isEmpty()) {
                                 overwritten = true;
                                 txtParcel.setText(writeOverStr);
                             }
                         }
                     } else {
                         newStr = newStr.replaceFirst("[_%]", "");
                     }
                     if (!newStr.isEmpty()) {
                         if (txtDistrict.getDocument().getLength() == 0) {
                             if (Character.isDigit(newStr.charAt(0))) {
                                 newStr = newStr.replaceAll("[^0-9]", "");
                             } else if (Character.isLetter(newStr.charAt(0))) {
                                 // removes all characters except alpha charactes and german vowel mutuations
                                 newStr = newStr.replaceAll(
                                         "[^\\u00F6\\u00D6\\u00FC\\u00DC\\u00E4\\u00C4\\u00dfa-zA-Z]",
                                         "");
                                 newStr = newStr.substring(0, Math.min(Math.max(2 - offs, 0), newStr.length()));
                                 if (validDistrict(
                                                 txtDistrict.getDocument().getText(
                                                     0,
                                                     txtDistrict.getDocument().getLength())
                                                 + newStr)) {
                                     if (changeFocus && ((offs + newStr.length()) >= 2)) {
                                         txtParcel.requestFocusInWindow();
                                     }
                                 } else {
                                     txtDistrict.setForeground(Color.red);
                                 }
                             }
                         } else if (txtDistrict.getDocument().getText(0, txtDistrict.getDocument().getLength()).matches(
                                         "^[0-9]*")) {
                             newStr = newStr.replaceAll("[^0-9]", ""); // checks if the entered text only contains
                                                                       // alpha charactes and german vowel mutuations
                         } else if (txtDistrict.getDocument().getText(0, txtDistrict.getDocument().getLength()).matches(
                                         "^[\\u00F6\\u00D6\\u00FC\\u00DC\\u00E4\\u00C4\\u00dfa-zA-Z]*")) {
                             // removes all characters except alpha charactes and german vowel mutuations
                             newStr = newStr.replaceAll(
                                     "[^\\u00F6\\u00D6\\u00FC\\u00DC\\u00E4\\u00C4\\u00dfa-zA-Z]",
                                     "");
                             newStr = newStr.substring(0, Math.min(Math.max(2 - offs, 0), newStr.length()));
 
                             if (validDistrict(
                                             txtDistrict.getDocument().getText(0, txtDistrict.getDocument().getLength())
                                             + newStr)) {
                                 if (changeFocus && ((offs + newStr.length()) >= 2)) {
                                     txtParcel.requestFocusInWindow();
                                 }
                             } else {
                                 txtDistrict.setForeground(Color.red);
                             }
                         }
                     }
                     super.insertString(offs, newStr, a);
                     updateParcel();
                 }
 
                 @Override
                 public void remove(final int offs, final int len) throws BadLocationException {
                     txtDistrict.setForeground(Color.BLACK);
                     super.remove(offs, len);
                 }
             });
 
         txtParcel.setDocument(new PlainDocument() {
 
                 @Override
                 public void insertString(final int offs, String str, final AttributeSet a) throws BadLocationException {
                     if (str == null) {
                         return;
                     }
                     str = str.replaceAll("[^-0-9_%]", "");
                     String newStr = str;
                     if ((str.length() > 0)
                                 && ((str.length() > (config.getMaxLenParcelNumberField() - offs))
                                     || str.substring(
                                         0,
                                         Math.min(config.getMaxLenParcelNumberField() + 1 - offs, str.length()))
                                     .contains(config.getDelimiter1AsString()))) {
                         int pos = str.indexOf(config.getDelimiter1());
                         if ((pos < 0) || (pos > (config.getMaxLenParcelNumberField() - offs))) {
                             pos = config.getMaxLenParcelNumberField() - offs;
                         }
                         if (changeFocus) {
                             txtLandParcelNumerator.requestFocusInWindow();
                         }
 
                         newStr = str.substring(0, pos);
 
                         if (writeOver) {
                             String writeOverStr;
                             if (str.indexOf(config.getDelimiter1AsString()) == pos) {
                                 writeOverStr = str.substring(pos + 1);
                             } else {
                                 writeOverStr = str.substring(pos);
                             }
                             if (!writeOverStr.isEmpty()) {
                                 overwritten = true;
                                 txtLandParcelNumerator.setText(writeOverStr);
                             }
                         }
                     }
                     super.insertString(offs, newStr, a);
                     updateParcel();
                 }
             });
 
         txtLandParcelNumerator.setDocument(new PlainDocument() {
 
                 @Override
                 public void insertString(final int offs, String str, final AttributeSet a) throws BadLocationException {
                     if (str == null) {
                         return;
                     }
                     str = str.replaceAll("[^-0-9_%]", "");
                     String newStr = str;
                     if ((str.length() > 0)
                                 && ((str.length() > (config.getMaxLenParcelNumeratorField() - offs))
                                     || str.substring(
                                         0,
                                         Math.min(config.getMaxLenParcelNumeratorField() + 1 - offs, str.length()))
                                     .contains(config.getDelimiter1AsString())
                                     || str.substring(
                                         0,
                                         Math.min(config.getMaxLenParcelNumeratorField() + 1 - offs, str.length()))
                                     .contains(config.getDelimiter2AsString()))) {
                         overwritten = true;
                         int pos = str.indexOf(config.getDelimiter1());
                         if ((pos < 0)
                                     || ((pos > str.indexOf(config.getDelimiter2()))
                                         && (str.indexOf(config.getDelimiter2()) >= 0))) {
                             pos = str.indexOf(config.getDelimiter2());
                         }
                         if ((pos < 0)
                                     || ((pos > (config.getMaxLenParcelNumeratorField() - offs))
                                         && ((config.getMaxLenParcelNumeratorField() - offs) >= 0))) {
                             pos = config.getMaxLenParcelNumeratorField() - offs;
                         }
                         if (changeFocus) {
                             txtLandParcelDenominator.requestFocusInWindow();
                         }
                         newStr = str.substring(0, pos);
 
                         if (writeOver) {
                             String writeOverStr;
                             if ((str.indexOf(config.getDelimiter1AsString()) == pos)
                                         || (str.indexOf(config.getDelimiter2AsString()) == pos)) {
                                 writeOverStr = str.substring(pos + 1);
                             } else {
                                 writeOverStr = str.substring(pos);
                             }
                             if (!writeOverStr.isEmpty()) {
                                 overwritten = true;
                                 txtLandParcelDenominator.setText(writeOverStr);
                             }
                         }
                     }
                     super.insertString(offs, newStr, a);
                     updateParcel();
                 }
             });
 
         txtLandParcelDenominator.setDocument(new PlainDocument() {
 
                 @Override
                 public void insertString(final int offs, String str, final AttributeSet a) throws BadLocationException {
                     if ((str == null) || str.isEmpty()) {
                         return;
                     }
                     str = str.replaceAll("[^0-9_%]", "");
                     if ((offs + str.length()) > config.getMaxLenParcelDenominatorField()) {
                         str = str.substring(0, config.getMaxLenParcelDenominatorField() - offs);
                     }
                     super.insertString(offs, str, a);
                     updateParcel();
                 }
             });
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean getValidParcelNr() {
         return validParcelNr;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void checkValidParcelNr() {
         final String[] parts = currentParcel.split("[-/]");
         if ((parts.length < 3) || (parts.length > 4)) {
             validParcelNr = false;
             return;
         }
         if (parts[0].matches("^05")) {
             if (!parts[0].matches("^05[0-9]{" + config.getMaxLenDistrictNumberField() + "}$")) {
                 validParcelNr = false;
                 return;
             }
         } else {
             if (!parts[0].matches("^[0-9]{" + config.getMaxLenDistrictNumberField() + "}$")) {
                 validParcelNr = false;
                 return;
             }
         }
         if (parts[1].contains("%")) {
             if (!parts[1].matches("^[0-9_%]{1," + config.getMaxLenParcelNumberField() + "}$")) {
                 validParcelNr = false;
                 return;
             }
         } else {
             if (!parts[1].matches("^[0-9_]{" + config.getMaxLenParcelNumberField() + "}$")) {
                 validParcelNr = false;
                 return;
             }
         }
         if (parts[2].contains("%")) {
             if (!parts[2].matches("^[0-9_%]{1," + config.getMaxLenParcelNumeratorField() + "}$")) {
                 validParcelNr = false;
                 return;
             }
         } else {
             if (!parts[2].matches("^[0-9_]{" + config.getMaxLenParcelNumeratorField() + "}$")) {
                 validParcelNr = false;
                 return;
             }
         }
         if (parts.length == 4) {
             if (parts[3].contains("%")) {
                 if (!parts[3].matches("^[0-9_%]{1," + config.getMaxLenParcelDenominatorField() + "}$")) {
                     validParcelNr = false;
                     return;
                 }
             } else {
                 if (!parts[3].matches("^[0-9_]{" + config.getMaxLenParcelDenominatorField() + "}$")) {
                     validParcelNr = false;
                     return;
                 }
             }
         }
         validParcelNr = true;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getDistrictName() {
         return districtName;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getDistrictNumber() {
         return districtNumber;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  districtNumber  DOCUMENT ME!
      */
     public void setDistrictNumber(final String districtNumber) {
         changeFocus = false;
         writeOver = false;
         txtDistrict.setText(districtNumber);
         changeFocus = true;
         writeOver = true;
         finishDistrict();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getCurrentParcel() {
         return currentParcel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  currentParcel  DOCUMENT ME!
      */
     public void setCurrentParcel(final String currentParcel) {
         changeFocus = false;
         txtDistrict.setText(currentParcel);
         finishDistrict();
         finishParcel();
         finishParcelNumerator();
         finishParcelDenominator();
         changeFocus = true;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getParcelDenominator() {
         return parcelDenominator;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  parcelDenominator  DOCUMENT ME!
      */
     public void setParcelDenominator(final String parcelDenominator) {
         changeFocus = false;
         writeOver = false;
         txtLandParcelDenominator.setText(parcelDenominator);
         finishParcelDenominator();
         changeFocus = true;
         writeOver = true;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getParcelNumber() {
         return parcelNumber;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  parcelNumber  DOCUMENT ME!
      */
     public void setParcelNumber(final String parcelNumber) {
         changeFocus = false;
         writeOver = false;
         txtParcel.setText(parcelNumber);
         finishParcel();
         changeFocus = true;
         writeOver = true;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getParcelNumerator() {
         return parcelNumerator;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  parcelNumerator  DOCUMENT ME!
      */
     public void setParcelNumerator(final String parcelNumerator) {
         changeFocus = false;
         writeOver = false;
         txtLandParcelNumerator.setText(parcelNumerator);
         finishParcelNumerator();
         changeFocus = true;
         writeOver = true;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  document  DOCUMENT ME!
      * @param  maxLen    DOCUMENT ME!
      * @param  offs      DOCUMENT ME!
      */
     private void addLeadingZeroes(final Document document, final int maxLen, final int offs) {
         if (document.getLength() < maxLen) {
             for (int i = document.getLength(); i < maxLen; i++) {
                 try {
                     document.insertString(offs, "0", null);
                 } catch (final BadLocationException ex) {
                     Logger.getLogger(ParcelInputField.class.getName())
                             .log(Level.INFO, "Leading zero could not be inserted", ex);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  document  DOCUMENT ME!
      * @param  maxLen    DOCUMENT ME!
      */
     private void addLeadingZeroes(final Document document, final int maxLen) {
         addLeadingZeroes(document, maxLen, 0);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void updateParcel() {
         final String oldParcel = currentParcel;
         final StringBuilder sb = new StringBuilder();
 
         if ((txtDistrict.getText() != null) && !txtDistrict.getText().isEmpty()) {
             sb.append(txtDistrict.getText());
         }
         if ((txtParcel.getText() != null) && !txtParcel.getText().isEmpty()) {
             sb.append(config.getDelimiter1AsString()).append(txtParcel.getText());
         }
         if ((txtLandParcelNumerator.getText() != null) && !txtLandParcelNumerator.getText().isEmpty()) {
             sb.append(config.getDelimiter1AsString()).append(txtLandParcelNumerator.getText());
         }
         if ((txtLandParcelDenominator.getText() != null) && !txtLandParcelDenominator.getText().isEmpty()
                     && !txtLandParcelDenominator.getText().matches("^0+$")) {
             sb.append(config.getDelimiter2AsString()).append(txtLandParcelDenominator.getText());
         }
 
         currentParcel = sb.toString();
         super.firePropertyChange(PROP_CURRENT_PARCEL, oldParcel, currentParcel);
         final boolean oldValid = validParcelNr;
         checkValidParcelNr();
         if (oldValid != validParcelNr) {
             super.firePropertyChange(PROP_VALID_PARCEL_NUMBER, oldValid, validParcelNr);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  blockNr  DOCUMENT ME!
      */
     private void fireAreaBlockFinished(final BlockType blockNr) {
         String oldValue;
         switch (blockNr) {
             case DISTRICT: {
                 oldValue = districtNumber;
                 districtNumber = txtDistrict.getText();
                 super.firePropertyChange(PROP_DISTRICT_NUMBER, oldValue, districtNumber);
                 break;
             }
             case PARCEL: {
                 oldValue = parcelNumber;
                 parcelNumber = txtParcel.getText();
                 super.firePropertyChange(PROP_PARCEL_NUMBER, oldValue, parcelNumber);
                 break;
             }
             case PARCEL_NUMERATOR: {
                 oldValue = parcelNumerator;
                 parcelNumerator = txtLandParcelNumerator.getText();
                 super.firePropertyChange(PROP_PARCEL_NUMERATOR, oldValue, parcelNumerator);
                 break;
             }
             case PARCEL_DENOMINATOR: {
                 oldValue = parcelDenominator;
                 parcelDenominator = txtLandParcelDenominator.getText();
                 super.firePropertyChange(PROP_PARCEL_DENOMINATOR, oldValue, parcelDenominator);
                 break;
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   districtAbrv  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Integer getDistrictNrFromAbrv(final String districtAbrv) {
         return config.getConversionMap()
                     .get(districtAbrv.substring(0, Math.min(2, districtAbrv.length())).toLowerCase());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   districtAbrv  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean validDistrict(final String districtAbrv) {
         return getDistrictNrFromAbrv(districtAbrv) != null;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void finishDistrict() {
         String text = txtDistrict.getText();
         if (text.matches("^[öÖüÜäÄßa-zA-Z]+$")) {
             if (validDistrict(text)) {
                 txtDistrict.setText(getDistrictNrFromAbrv(text).toString());
                 text = txtDistrict.getText();
             } else {
                 txtDistrict.setForeground(Color.red);
             }
         }
         if (text.matches("^[0-9]+$") || text.isEmpty()) {
             if (!text.matches("^05.*")) // Leading 05 in districtNr
             {
                 txtDistrict.setText("05" + text);
             }
             addLeadingZeroes(txtDistrict.getDocument(), config.getMaxLenDistrictNumberField() + 2, 2);
         }
         int intArea;
         final String oldAreaName = districtName;
         try {
             if (txtDistrict.getText().length() > config.getMaxLenDistrictNumberField()) {
                 intArea = Integer.parseInt(txtDistrict.getText().substring(2));
             } else {
                 intArea = Integer.parseInt(txtDistrict.getText().substring(0));
             }
             districtName = config.getDistrictNamesMap().get(intArea);
         } catch (final NumberFormatException e) {
             districtName = null;
             // dont care, simply unknownn district, maybe insert log
         }
         firePropertyChange(PROP_DISTRICT_NAME, oldAreaName, districtName);
         if (districtName != null) {
             fireAreaBlockFinished(BlockType.DISTRICT);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void finishParcel() {
         if (!txtParcel.getText().contains("%")) {
             addLeadingZeroes(txtParcel.getDocument(), config.getMaxLenParcelNumberField());
         }
         fireAreaBlockFinished(BlockType.PARCEL);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void finishParcelNumerator() {
         if (!txtLandParcelNumerator.getText().contains("%")) {
             addLeadingZeroes(txtLandParcelNumerator.getDocument(), config.getMaxLenParcelNumeratorField());
         }
         fireAreaBlockFinished(BlockType.PARCEL_NUMERATOR);
     }
 
     /**
      * DOCUMENT ME!
      */
     private void finishParcelDenominator() {
         final String text = txtLandParcelDenominator.getText();
         if ((text != null) && !text.isEmpty() && !txtLandParcelDenominator.getText().contains("%")) {
             addLeadingZeroes(txtLandParcelDenominator.getDocument(), config.getMaxLenParcelDenominatorField());
         }
         fireAreaBlockFinished(BlockType.PARCEL_DENOMINATOR);
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         txtDistrict = new javax.swing.JTextField();
         lblDelimitier1 = new javax.swing.JLabel(config.getDelimiter1AsString());
         txtParcel = new javax.swing.JTextField();
         lblDelimitier2 = new javax.swing.JLabel(config.getDelimiter1AsString());
         txtLandParcelNumerator = new javax.swing.JTextField();
         lblDelimitier3 = new javax.swing.JLabel(config.getDelimiter2AsString());
         txtLandParcelDenominator = new javax.swing.JTextField();
 
         setLayout(new java.awt.GridBagLayout());
 
         txtDistrict.setHorizontalAlignment(javax.swing.JTextField.CENTER);
         FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(txtDistrict.getFont());
        Dimension dim = new Dimension(metrics.stringWidth("0") * (config.getMaxLenDistrictNumberField()+4), metrics.getHeight()+4);
         txtDistrict.setMinimumSize(dim);
         txtDistrict.setPreferredSize(dim);
         txtDistrict.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 txtDistrictFocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 txtDistrictFocusLost(evt);
             }
         });
         add(txtDistrict, new java.awt.GridBagConstraints());
 
         lblDelimitier1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
         add(lblDelimitier1, gridBagConstraints);
 
         txtParcel.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        dim = new Dimension(metrics.stringWidth("0") * (config.getMaxLenParcelNumberField()+2), metrics.getHeight()+4);
         txtParcel.setMinimumSize(dim);
         txtParcel.setPreferredSize(dim);
         txtParcel.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 txtParcelFocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 txtParcelFocusLost(evt);
             }
         });
         add(txtParcel, new java.awt.GridBagConstraints());
 
         lblDelimitier2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
         add(lblDelimitier2, gridBagConstraints);
 
         txtLandParcelNumerator.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        dim = new Dimension(metrics.stringWidth("0") * (config.getMaxLenParcelNumeratorField()+2), metrics.getHeight()+4);
         txtLandParcelNumerator.setMinimumSize(dim);
         txtLandParcelNumerator.setPreferredSize(dim);
         txtLandParcelNumerator.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 txtLandParcelNumeratorFocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 txtLandParcelNumeratorFocusLost(evt);
             }
         });
         add(txtLandParcelNumerator, new java.awt.GridBagConstraints());
 
         lblDelimitier3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
         add(lblDelimitier3, gridBagConstraints);
 
         txtLandParcelDenominator.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        dim = new Dimension(metrics.stringWidth("0") * (config.getMaxLenParcelDenominatorField()+2), metrics.getHeight()+4);
         txtLandParcelDenominator.setMinimumSize(dim);
         txtLandParcelDenominator.setPreferredSize(dim);
         txtLandParcelDenominator.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 txtLandParcelDenominatorFocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 txtLandParcelDenominatorFocusLost(evt);
             }
         });
         add(txtLandParcelDenominator, new java.awt.GridBagConstraints());
     }// </editor-fold>//GEN-END:initComponents
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtDistrictFocusLost(final java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDistrictFocusLost
         finishDistrict();
         txtDistrict.setCaretPosition(0);
     }//GEN-LAST:event_txtDistrictFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtParcelFocusLost(final java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtParcelFocusLost
         finishParcel();
         txtParcel.setCaretPosition(0);
     }//GEN-LAST:event_txtParcelFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtLandParcelNumeratorFocusLost(final java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtLandParcelNumeratorFocusLost
         finishParcelNumerator();
         txtLandParcelNumerator.setCaretPosition(0);
     }//GEN-LAST:event_txtLandParcelNumeratorFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtLandParcelDenominatorFocusLost(final java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtLandParcelDenominatorFocusLost
         finishParcelDenominator();
         txtLandParcelDenominator.setCaretPosition(0);
     }//GEN-LAST:event_txtLandParcelDenominatorFocusLost
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtLandParcelDenominatorFocusGained(final java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtLandParcelDenominatorFocusGained
         final int textLenght = txtLandParcelDenominator.getText().length();
         if (overwritten) {
             overwritten = false;
             txtLandParcelDenominator.getHighlighter().removeAllHighlights();
             txtLandParcelDenominator.setCaretPosition(textLenght);
         } else {
             txtLandParcelDenominator.setCaretPosition(0);
             txtLandParcelDenominator.moveCaretPosition(textLenght);
         }
     }//GEN-LAST:event_txtLandParcelDenominatorFocusGained
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtLandParcelNumeratorFocusGained(final java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtLandParcelNumeratorFocusGained
         final int textLength = txtLandParcelNumerator.getText().length();
         if (overwritten) {
             overwritten = false;
             txtLandParcelNumerator.getHighlighter().removeAllHighlights();
             txtLandParcelNumerator.setCaretPosition(textLength);
         } else {
             txtLandParcelNumerator.setCaretPosition(0);
             txtLandParcelNumerator.moveCaretPosition(textLength);
         }
     }//GEN-LAST:event_txtLandParcelNumeratorFocusGained
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtParcelFocusGained(final java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtParcelFocusGained
         final int textLength = txtParcel.getText().length();
         if (overwritten) {
             overwritten = false;
             txtParcel.getHighlighter().removeAllHighlights();
             txtParcel.setCaretPosition(textLength);
         } else {
             txtParcel.setCaretPosition(0);
             txtParcel.moveCaretPosition(textLength);
         }
     }//GEN-LAST:event_txtParcelFocusGained
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void txtDistrictFocusGained(final java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDistrictFocusGained
         final int textLength = txtDistrict.getText().length();
         if (overwritten) {
             overwritten = false;
             txtDistrict.getHighlighter().removeAllHighlights();
             txtDistrict.setCaretPosition(textLength);
         } else {
             txtDistrict.setCaretPosition(0);
             txtDistrict.moveCaretPosition(textLength);
         }
     }//GEN-LAST:event_txtDistrictFocusGained
 }
