 package fr.nantes1900.view.isletprocess;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.text.DecimalFormat;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import fr.nantes1900.constants.Icones;
 import fr.nantes1900.constants.TextsKeys;
 import fr.nantes1900.constants.coefficients.SeparationBuildings;
 import fr.nantes1900.constants.coefficients.SeparationGroundBuilding;
 import fr.nantes1900.constants.coefficients.SeparationWallRoof;
 import fr.nantes1900.constants.coefficients.SeparationWallsSeparationRoofs;
 import fr.nantes1900.constants.coefficients.SimplificationSurfaces;
 import fr.nantes1900.models.islets.buildings.AbstractBuildingsIslet;
 import fr.nantes1900.utils.FileTools;
 
 /**
  * Implements a panel displaying the parameters of the processes. The user can
  * then change them.
  * @author Luc Jallerat
  */
 public class ParametersView extends JPanel {
 
     /**
      * Serial version UID.
      */
     private static final long serialVersionUID = 1L;
 
     /**
      * The list of labels displaying the names of the parameters.
      */
     private JLabel[] property;
     /**
      * The list of values displaying the parameters.
      */
     private ValueProperty[] value;
     /**
      * The button save.
      */
     private JButton bSave = new JButton(new ImageIcon(Icones.save));
     /**
      * The button load.
      */
     private JButton bLoad = new JButton(new ImageIcon(Icones.open));
     /**
      * The button show.
      */
     private JButton bShow = new JButton(new ImageIcon(Icones.showProperties));
 
     /**
      * TODO by Luc.
      */
     private JPanel pTop;
     /**
      * TODO by Luc.
      */
     private JPanel pCenter;
     /**
      * TODO by Luc.
      */
     private JPanel pBottom;
 
     /**
      * The dimension of the values.
      */
     private final Dimension valueDimension = new Dimension(50, 30);
     /**
      * The dimension of the labels.
      */
     private final Dimension labelDimension = new Dimension(140, 30);
 
     /**
      * Constructor.
      */
     public ParametersView() {
 
         // Init parameters
         this.property = new JLabel[15];
         this.value = new ValueProperty[15];
         this.property[1] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_ALTITUDEERROR));
         this.value[1] = new ValueProperty(
                 SeparationGroundBuilding.getAltitureError());
         this.property[2] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_ANGLEGROUNDERROR));
         this.value[2] = new ValueProperty(
                 SeparationGroundBuilding.getAngleGroundError());
         this.property[3] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_LARGEANGLEGROUNDERROR));
         this.value[3] = new ValueProperty(
                 SeparationGroundBuilding.getLargeAngleGroundError());
         this.property[4] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_BLOCKGROUNDSSIZEERROR));
         this.value[4] = new ValueProperty(
                 SeparationGroundBuilding.getBlockGroundsSizeError());
         this.property[5] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_BLOCKBUILDINGSIZE));
         this.value[5] = new ValueProperty(
                 SeparationBuildings.getBlockBuildingSize());
         this.property[6] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_NORMALTOERROR));
         this.value[6] = new ValueProperty(SeparationWallRoof.getNormalToError());
         this.property[7] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_LARGEANGLEERROR));
         this.value[7] = new ValueProperty(
                 SeparationWallsSeparationRoofs.getLargeAngleError());
         this.property[8] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_MIDDLEANGLEERROR));
         this.value[8] = new ValueProperty(
                 SeparationWallsSeparationRoofs.getMiddleAngleError());
         this.property[9] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_PLANESERROR));
         this.value[9] = new ValueProperty(
                 SeparationWallsSeparationRoofs.getPlanesError());
         this.property[10] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_ROOFANGLEERROR));
         this.value[10] = new ValueProperty(
                 SeparationWallsSeparationRoofs.getRoofAngleError());
         this.property[11] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_ROOFSIZEERROR));
         this.value[11] = new ValueProperty(
                 SeparationWallsSeparationRoofs.getRoofSizeError());
         this.property[12] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_WALLANGLEERROR));
         this.value[12] = new ValueProperty(
                 SeparationWallsSeparationRoofs.getWallAngleError());
         this.property[13] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_WALLSIZEERROR));
         this.value[13] = new ValueProperty(
                 SeparationWallsSeparationRoofs.getWallSizeError());
         this.property[14] = new JLabel(
                 FileTools.readElementText(TextsKeys.KEY_ISORIENTEDFACTOR));
         this.value[14] = new ValueProperty(
                 SimplificationSurfaces.getIsOrientedFactor());
 
         // Dimensions of the formatted text fields
        for (int i = 1; i <= this.property.length; i++) {
             this.property[i].setPreferredSize(this.labelDimension);
         }
 
         // Layout
         this.setLayout(new BorderLayout());
         this.pTop = new JPanel();
         this.pCenter = new JPanel();
         this.pBottom = new JPanel();
         this.add(this.pTop, BorderLayout.NORTH);
         this.add(this.pCenter, BorderLayout.CENTER);
         this.add(this.pBottom, BorderLayout.SOUTH);
 
         // Displays the parameters
         this.displayParameters(AbstractBuildingsIslet.FIRST_STEP);
 
         // Tooltips of the buttons
         this.bSave.setToolTipText(FileTools
                 .readElementText(TextsKeys.KEY_SAVEPARAMETERSBUTTON));
         this.bLoad.setToolTipText(FileTools
                 .readElementText(TextsKeys.KEY_LOADPARAMETERSBUTTON));
         this.bShow.setToolTipText(FileTools
                 .readElementText(TextsKeys.KEY_SHOWPARAMETERSBUTTON));
 
         // Displays the buttons
         this.pTop.setLayout(new FlowLayout(FlowLayout.RIGHT));
         this.pTop.add(this.bLoad);
         this.pTop.add(this.bSave);
         this.pBottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
         this.pBottom.add(this.bShow);
     }
 
     /**
      * TODO by Luc.
      * @param x
      *            TODO by Luc.
      * @param y
      *            TODO by Luc.
      * @param n
      *            TODO by Luc.
      */
     private void displayOneParameter(final int x, final int y, final int n) {
         this.pCenter.add(this.property[n], new GridBagConstraints(x, y, 1, 1,
                 0, 0, GridBagConstraints.PAGE_START,
                 GridBagConstraints.HORIZONTAL, new Insets(8, 8, 8, 8), 0, 5));
         this.pCenter.add(this.value[n], new GridBagConstraints(x + 1, y, 1, 1,
                 0, 0, GridBagConstraints.PAGE_START,
                 GridBagConstraints.HORIZONTAL, new Insets(8, 8, 8, 8), 0, 5));
     }
 
     /**
      * Displays the parameters corresponding to the current step.
      * @param i
      *            the number of the current step
      */
     public final void displayParameters(final int i) {
         this.pCenter.removeAll();
         this.pCenter.setLayout(new GridBagLayout());
         switch (i) {
         case 1:
             this.displayOneParameter(0, 0, 1);
             this.displayOneParameter(0, 1, 2);
             this.displayOneParameter(0, 2, 3);
             this.displayOneParameter(0, 3, 4);
             break;
         case 2:
             this.displayOneParameter(0, 0, 5);
             break;
         case 3:
             this.displayOneParameter(0, 0, 6);
             break;
         case 4:
             this.displayOneParameter(0, 0, 7);
             this.displayOneParameter(0, 1, 8);
             this.displayOneParameter(0, 2, 9);
             this.displayOneParameter(0, 3, 10);
             this.displayOneParameter(0, 4, 11);
             this.displayOneParameter(0, 5, 12);
             this.displayOneParameter(0, 6, 13);
             break;
         case 5:
             this.displayOneParameter(0, 0, 14);
             break;
         default:
             break;
         }
     }
 
     /**
      * Computes the width.
      * @return the preferred width
      */
     public final int getPreferredWidth() {
         // width of each + little margin
         return this.valueDimension.width + this.labelDimension.width + 10;
     }
 
     /**
      * Getter.
      * @param i
      *            the number of the parameter
      * @return the value currently displayed
      */
     public final double getValueProperty(final int i) {
         return ((Number) (this.value[i]).getValue()).doubleValue();
     }
 
     /**
      * Setter.
      * @param i
      *            the number of the parameter
      * @param newValue
      *            the new value to display
      */
     public final void setValueProperty(final int i, final double newValue) {
         this.value[i].setValue(newValue);
     }
 
     /**
      * Getter.
      * @return the save button
      */
     public final JButton getSaveButton() {
         return this.bSave;
     }
 
     /**
      * Getter.
      * @return the load button
      */
     public final JButton getLoadButton() {
         return this.bLoad;
     }
 
     /**
      * Getter.
      * @return the show button
      */
     public final JButton getShowButton() {
         return this.bShow;
     }
 
     /**
      * @author Luc Jallerat
      */
     private class ValueProperty extends JFormattedTextField {
 
         /**
          * Serial version UID.
          */
         private static final long serialVersionUID = 1L;
 
         /**
          * Constructor.
          * @param f
          *            the double value
          */
         public ValueProperty(final double f) {
             super(new DecimalFormat());
             this.setValue(f);
             this.setPreferredSize(ParametersView.this.valueDimension);
         }
     }
 }
