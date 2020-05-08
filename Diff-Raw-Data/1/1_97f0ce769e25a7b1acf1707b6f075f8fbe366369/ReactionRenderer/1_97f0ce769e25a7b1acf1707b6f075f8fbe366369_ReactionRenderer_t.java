 /**
  * ReactionRenderer.java
  *
  * 2011.09.27
  *
  * This file is part of the CheMet library
  * 
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.chemet.render.reaction;
 
 import com.google.common.collect.BiMap;
 import java.awt.*;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.security.InvalidParameterException;
 import java.util.Arrays;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import org.apache.log4j.Logger;
 import org.openscience.cdk.Molecule;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.layout.StructureDiagramGenerator;
 import org.openscience.cdk.renderer.AtomContainerRenderer;
 import org.openscience.cdk.renderer.font.AWTFontManager;
 import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
 import org.openscience.cdk.renderer.generators.BasicBondGenerator;
 import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
 import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
 import uk.ac.ebi.chemet.entities.reaction.AbstractReaction;
 import uk.ac.ebi.interfaces.entities.Metabolite;
 import uk.ac.ebi.core.tools.TransportReactionUtil;
 import uk.ac.ebi.core.tools.TransportReactionUtil.*;
 import uk.ac.ebi.chemet.render.ViewUtilities;
 import uk.ac.ebi.core.reaction.MetabolicParticipantImplementation;
 import uk.ac.ebi.interfaces.entities.MetabolicParticipant;
 import uk.ac.ebi.interfaces.entities.Reaction;
 import uk.ac.ebi.interfaces.reaction.Compartment;
 import uk.ac.ebi.interfaces.reaction.CompartmentalisedParticipant;
 import uk.ac.ebi.interfaces.reaction.Direction;
 
 
 /**
  *          ReactionRenderer – 2011.09.27 <br>
  *          Class description
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  */
 public class ReactionRenderer {
 
     private static final Logger LOGGER = Logger.getLogger(ReactionRenderer.class);
 
     private AtomContainerRenderer renderer =
                                   new AtomContainerRenderer(
             Arrays.asList(new BasicSceneGenerator(),
                           new BasicBondGenerator(),
                           new BasicAtomGenerator()),
             new AWTFontManager());
 
     private Map<TransportReactionUtil.Classification, ImageIcon> tClassMap = new EnumMap<TransportReactionUtil.Classification, ImageIcon>(
             TransportReactionUtil.Classification.class);
 
 
     public ReactionRenderer() {
 
         tClassMap.put(Classification.UNKNOWN, ViewUtilities.getIcon("images/classification/noport.png"));
         tClassMap.put(Classification.SYMPORTER, ViewUtilities.getIcon("images/classification/symport.png"));
         tClassMap.put(Classification.ANTIPORTER, ViewUtilities.getIcon("images/classification/antiport.png"));
         tClassMap.put(Classification.UNIPORTER, ViewUtilities.getIcon("images/classification/uniport.png"));
 
     }
 
 
     public ImageIcon renderTransportReaction(AbstractReaction<MetabolicParticipantImplementation> rxn) {
 
         if (!TransportReactionUtil.isTransport(rxn)) {
             throw new InvalidParameterException("Provided reaction is not a transport reaction");
         }
 
 
         Classification classification = TransportReactionUtil.getClassification(rxn);
 
         switch (classification) {
             case ANTIPORTER:
                 return null;
             case SYMPORTER:
                 return null;
             case UNIPORTER:
                 return renderUniporterReaction(rxn);
         }
 
         return null;
 
     }
 
 
     /**
      * Draws a transport uniporter reaction
      * @param rxn
      * @return
      */
     public ImageIcon renderUniporterReaction(AbstractReaction<MetabolicParticipantImplementation> rxn) {
 
         BiMap<CompartmentalisedParticipant<Metabolite, ?, Compartment>, CompartmentalisedParticipant<Metabolite, ?, Compartment>> mapping = TransportReactionUtil.getMappings(
                 rxn);
 
         CompartmentalisedParticipant<Metabolite, ?, Compartment> left = mapping.keySet().iterator().next();
         CompartmentalisedParticipant<Metabolite, ?, Compartment> right = mapping.get(left);
 
         int n = mapping.size();
 
         int width = (n * 128) + (128); // arrow
         int height = 128;
 
         BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
         Graphics2D g2 = (Graphics2D) base.getGraphics();
         g2.setColor(Color.WHITE);
         g2.fill(new Rectangle(0, 0, width, height));
 
         drawMolecule(g2, new Rectangle(0, 0, 128, 128),
                      (MetabolicParticipantImplementation) left);
 
         drawCompartmentSeperator(g2, new Rectangle(128, 0, 128, 128));
 
         drawArrow(g2, new Rectangle(128, 0, 128, 128), rxn.getDirection(), 1.25f);
 
         drawMolecule(g2, new Rectangle(256, 0, 128, 128),
                      (MetabolicParticipantImplementation) right);
 
         g2.dispose();
 
         return new ImageIcon(base);
 
     }
 
 
     public void drawCompartmentSeperator(Graphics2D g2,
                                          Rectangle bounds) {
 
         double cx = bounds.getCenterX();
         double cy = bounds.getCenterY();
 
         g2.setColor(Color.LIGHT_GRAY);
         g2.setStroke(new BasicStroke(2f,
                                      BasicStroke.CAP_ROUND,
                                      BasicStroke.JOIN_ROUND,
                                      1f,
                                      new float[]{5f},
                                      2.5f));
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 
         GeneralPath lp = new GeneralPath();
         lp.moveTo(cx - 25, 5);
         lp.quadTo(cx - 5, 5, cx - 5, 25);
         lp.lineTo(cx - 5, bounds.getHeight() - 25);
         lp.quadTo(cx - 5, bounds.getHeight() - 5, cx - 25, bounds.getHeight() - 5);
 
         GeneralPath rp = new GeneralPath();
         rp.moveTo(cx + 25, 5);
         rp.quadTo(cx + 5, 5, cx + 5, 25);
         rp.lineTo(cx + 5, bounds.getHeight() - 25);
         rp.quadTo(cx + 5, bounds.getHeight() - 5, cx + 25, bounds.getHeight() - 5);
 
         g2.draw(lp);
 
         g2.draw(rp);
 
 
     }
 
 
     public ImageIcon getReaction(Reaction<MetabolicParticipant> rxn) {
 
         int n = rxn.getParticipants().size();
 
         if (n == 0) {
             return new ImageIcon();
         }
 
         int height = 128;
         int width = (n * 128)
                     + (128) // arrow
                     + (n > 2 ? ((n - 2) * 15) : 0);
 
         BufferedImage masterImg = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
         Graphics2D g2 = (Graphics2D) masterImg.getGraphics();
         g2.setColor(Color.WHITE);
         g2.fill(new Rectangle(0, 0, width, height));
 
         Rectangle2D bounds = new Rectangle2D.Double(-128, 0, 128, 128);
 
         List<MetabolicParticipant> reactants = rxn.getReactants();
         for (int i = 0; i < reactants.size(); i++) {
 
             bounds = new Rectangle2D.Double(bounds.getX() + bounds.getWidth(),
                                             0, 128, 128);
             BufferedImage subImage = new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);
             drawMolecule((Graphics2D) subImage.getGraphics(), new Rectangle(0, 0, 128, 128),
                          (MetabolicParticipantImplementation) reactants.get(i));
             g2.drawImage(subImage, (int) bounds.getX(), (int) bounds.getY(), null);
 
             if (i + 1 < reactants.size()) {
                 bounds = new Rectangle2D.Double(bounds.getX() + bounds.getWidth(),
                                                 0, 15, 128);
                 drawPlus(g2, bounds);
             }
         }
 
         bounds = new Rectangle2D.Double(bounds.getX() + bounds.getWidth(),
                                         0, 128, 128);
         drawArrow(g2, bounds, rxn.getDirection());
         List<MetabolicParticipant> products = rxn.getProducts();
         for (int i = 0; i < products.size(); i++) {
             bounds = new Rectangle2D.Double(bounds.getX() + bounds.getWidth(),
                                             0, 128, 128);
             BufferedImage subImage = new BufferedImage(128, 128, BufferedImage.TYPE_4BYTE_ABGR);
             drawMolecule((Graphics2D) subImage.getGraphics(), new Rectangle(0, 0, 128, 128),
                          (MetabolicParticipantImplementation) products.get(i));
             g2.drawImage(subImage, (int) bounds.getX(), (int) bounds.getY(), null);
             if (i + 1 < products.size()) {
                 bounds = new Rectangle2D.Double(bounds.getX() + bounds.getWidth(),
                                                 0, 15, 128);
                 drawPlus(g2, bounds);
             }
         }
 
         g2.dispose();
 
         return new ImageIcon(masterImg);
 
     }
 
 
     public void drawMolecule(Graphics2D g2,
                              Rectangle2D bounds,
                              MetabolicParticipantImplementation p) {
 
         Metabolite metabolite = p.getMolecule();
         String compartment = "[" + p.getCompartment().getAbbreviation() + "]";
        g2.setColor(Color.LIGHT_GRAY);
         g2.setFont(ViewUtilities.DEFAULT_MONO_SPACE_FONT.deriveFont(11.0f));
         int compartmentWidth = g2.getFontMetrics().stringWidth(compartment);
         int compartmentHeight = g2.getFontMetrics().getHeight();
         g2.drawString(compartment, (int) bounds.getWidth() - compartmentWidth, compartmentHeight);
 
 
         if (metabolite.hasStructure()) {
             IAtomContainer atomContainer = metabolite.getStructures().iterator().next().
                     getStructure();
 
             renderer.paint(atomContainer, new AWTDrawVisitor(g2), bounds, true);
             g2.dispose();
         } else {
             g2.setColor(Color.LIGHT_GRAY);
             g2.setFont(ViewUtilities.DEFAULT_BODY_FONT.deriveFont(18.0f));
             String na = "unavailable";
             int mW = g2.getFontMetrics().stringWidth(na);
             int mH = g2.getFontMetrics().getHeight();
 
             g2.drawString(na, (int) bounds.getCenterX() - (mW / 2), (int) bounds.getCenterY() + (mH / 2));
         }
 
     }
 
 
     public void drawPlus(Graphics2D g2,
                          Rectangle2D bounds) {
         double length = (bounds.getWidth() / 2) * 0.8;
 
         double centreX = (bounds.getWidth() / 2d) + bounds.getX();
         double centreY = (bounds.getHeight() / 2d) + bounds.getY();
 
         g2.setColor(Color.LIGHT_GRAY);
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         String direction = "+";
         g2.setFont(ViewUtilities.DEFAULT_BODY_FONT.deriveFont(34.0f));
         int width = g2.getFontMetrics().stringWidth(direction);
         int height = g2.getFontMetrics().getHeight();
         g2.drawString(direction, (int) bounds.getCenterX() - (width / 2), (int) bounds.getCenterY() + (height / 2));
 
     }
 
 
     public void drawArrow(Graphics2D g2,
                           Rectangle2D bounds,
                           Direction direction) {
         drawArrow(g2, bounds, direction, 1f);
     }
 
 
     public void drawArrow(Graphics2D g2,
                           Rectangle2D bounds,
                           Direction direction,
                           float scale) {
         double length = (bounds.getWidth() / 2) * 0.8;
 
 
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(Color.LIGHT_GRAY);
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 
         String symbol = direction.getSymbol();
         g2.setFont(ViewUtilities.DEFAULT_BODY_FONT.deriveFont(34.0f * scale));
         Rectangle2D sBounds = g2.getFontMetrics().getStringBounds(symbol, g2);
         int width = (int) sBounds.getWidth();
         int height = (int) sBounds.getHeight();
 
         g2.drawString(symbol, (int) bounds.getCenterX() - (width / 2),
                       (int) bounds.getCenterY() + (height / 2));
 
 
     }
 
 
     public ImageIcon getTransportClassificationIcon(TransportReactionUtil.Classification classification) {
         return tClassMap.get(classification);
     }
 
 
     public static void main(String[] args) throws IOException {
         BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR);
 //        Reaction rxn = new R
 //        new ReactionRenderer().getReaction()
         ImageIO.write(img, "png", new File("/Users/johnmay/Desktop/sample.png"));
     }
 }
