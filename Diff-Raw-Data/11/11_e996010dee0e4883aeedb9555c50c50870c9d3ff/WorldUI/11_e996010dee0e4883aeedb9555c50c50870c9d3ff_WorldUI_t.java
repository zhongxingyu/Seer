 package org.kornicameister.sise.lake.types.world.ui;
 
 import org.apache.log4j.Logger;
 import org.kornicameister.sise.lake.clisp.ClispEnvironment;
 import org.kornicameister.sise.lake.types.WorldField;
 import org.kornicameister.sise.lake.types.WorldHelper;
 import org.kornicameister.sise.lake.types.actors.DefaultActor;
 import org.kornicameister.sise.lake.types.actors.impl.kg.ForesterActorKG;
 import org.kornicameister.sise.lake.types.world.DefaultWorld;
 import org.kornicameister.sise.lake.types.world.impl.LakeWorld;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Created with IntelliJ IDEA.
  * User: GodDamnItNappa!
  * Date: 16.06.13
  * Time: 00:22
  * To change this template use File | Settings | File Templates.
  */
 public class WorldUI extends JFrame implements ActionListener {
     private final Logger LOGGER = Logger.getLogger(WorldUI.class);
     private final Integer SPINNER_MIN_VALUE = 1;
     private final Integer SPINNER_MAX_VALUE = 100;
     private final Integer SPINNER_STEP_VALUE = 1;
     private final Integer SPINNER_INIT_VALUE = 1;
     private int maxFieldsHorizontally;
     private int maxFieldsVertically;
     private JPanel panel = null;
     private List<Field> fields = null;
     private JButton nextRound;
     private JSpinner numberAutoNextRound;
     private JCheckBox enableStorm;
     private ClispEnvironment environment;
 
     public WorldUI(String worldProp, String propFile) {
         this.environment = ClispEnvironment.getInstance(worldProp);
         //test
         maxFieldsHorizontally = WorldHelper.getWorld().getWidth();
         maxFieldsVertically = WorldHelper.getWorld().getHeight();
         //panel setting
         this.panel = new JPanel(null);
         this.fields = new ArrayList<>();
         this.setLayout(null);
         this.setImages(propFile);
         this.addFields();
         this.generateWorld();
         this.nextRound = new JButton("Next round");
         this.nextRound.setBounds(maxFieldsHorizontally * this.fields.get(0).getLabel().getSize().width +
                 (this.fields.get(0).getLabel().getSize().width / 2), 0, 100, 30);
         this.nextRound.addActionListener(this);
         this.nextRound.setVisible(true);
         this.panel.add(nextRound);
         SpinnerModel model = new SpinnerNumberModel(SPINNER_INIT_VALUE, SPINNER_MIN_VALUE, SPINNER_MAX_VALUE, SPINNER_STEP_VALUE);
         this.numberAutoNextRound = new JSpinner(model);
         this.numberAutoNextRound.setBounds((int) this.nextRound.getBounds().getX(), ((int) (this.nextRound.getBounds().getHeight() * 1.2)), 100, 20);
         //this.numberAutoNextRound.addActionListener(this);
         this.numberAutoNextRound.setVisible(true);
         this.panel.add(numberAutoNextRound);
         this.enableStorm = new JCheckBox("Storm");
         this.enableStorm.setBounds((int) this.nextRound.getBounds().getX(),
                 ((int) ((this.numberAutoNextRound.getBounds().getY() + this.numberAutoNextRound.getBounds().getHeight()) * 1.2)), 100, 20);
         this.enableStorm.setVisible(true);
         this.panel.add(enableStorm);
         panel.setSize(
                 (maxFieldsHorizontally + 1) * this.fields.get(0).getLabel().getSize().width + this.nextRound.getWidth(),
                 maxFieldsVertically * this.fields.get(0).getLabel().getSize().height + (this.fields.get(0).getLabel().getSize().height / 2));
         //window setting
         this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         this.setLocationRelativeTo(null);
         this.setResizable(false);
         this.setSize(panel.getSize());
         this.setContentPane(panel);
         panel.setVisible(true);
         this.setVisible(true);
         this.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 if (WorldUI.this.environment.isBootstrapped()) {
                     WorldUI.this.environment.destroy();
                 }
             }
         });
     }
 
     private void setImages(String propFile) {
         Properties properties = new Properties();
         try {
             properties.load(new BufferedReader(new FileReader(new File(propFile))));
             Field.ANGLER_IMAGE = properties.getProperty("ANGLER_IMAGE");
             Field.BIRD_IMAGE = properties.getProperty("BIRD_IMAGE");
             Field.FORESTER_IMAGE = properties.getProperty("FORESTER_IMAGE");
             Field.HERBIVORE_FISH_IMAGE = properties.getProperty("HERBIVORE_FISH_IMAGE");
             Field.POACHER_IMAGE = properties.getProperty("POACHER_IMAGE");
             Field.PREDATOR_FISH_IMAGE = properties.getProperty("PREDATOR_FISH_IMAGE");
             Field.LAND_IMAGE = properties.getProperty("LAND_IMAGE");
             Field.WATER_IMAGE = properties.getProperty("WATER_IMAGE");
 
         } catch (IOException e) {
             LOGGER.fatal(String.format("Failure when reading from properties file -> %s", propFile), e);
             return;
         }
     }
 
     public void addFields() {
         for (int i = 0; i < maxFieldsVertically; i++) {
             for (int j = 0; j < maxFieldsHorizontally; j++) {
                 // System.out.println(i + " " + j);
                 this.fields.add(0, new Field(new JLabel(Integer.toString(i + j)), new ForesterActorKG()));
                 this.fields.get(0).getLabel().setBounds(
                         this.fields.get(0).getLabel().getIcon().getIconWidth() * j,
                         this.fields.get(0).getLabel().getIcon().getIconHeight() * i,
                         this.fields.get(0).getLabel().getIcon().getIconWidth(),
                         this.fields.get(0).getLabel().getIcon().getIconHeight());
                 panel.add(this.fields.get(0).getLabel());
             }
         }
         //System.out.println(panel.getComponentCount());
         Collections.reverse(this.fields);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() instanceof JButton) {
             if (environment.isBootstrapped()) {
                 DefaultWorld world = WorldHelper.getWorld();
                 if (world instanceof LakeWorld) {
                     if (enableStorm.isSelected()) {
                         ((LakeWorld) world).applyWeather(LakeWorld.Weather.RAIN, LakeWorld.Weather.STORM, LakeWorld.Weather.SUN);
                     } else
                         ((LakeWorld) world).applyWeather(LakeWorld.Weather.RAIN, LakeWorld.Weather.SUN);
                 }
                 for (int i = 0; i < (Integer) this.numberAutoNextRound.getValue(); i++) {
                     environment.mainLoop();
                     System.out.print("Next tour -> \t");
                     this.generateWorld();
                 }
                 numberAutoNextRound.setValue(SPINNER_INIT_VALUE);
             }
         }
     }
 
     private void generateWorld() {
         for (Field field : fields) {
             int x = field.getLabel().getX() /
                     field.getLabel().getIcon().getIconHeight();
             int y = field.getLabel().getY() /
                     field.getLabel().getIcon().getIconWidth();
             WorldField next = WorldHelper.getField(x, y);
            field.setWater(next.isWater());
            DefaultActor actor = WorldHelper.getActor(next);
            if (actor.isAlive() != null && actor.isAlive())
                field.setActor(actor);
         }
     }
 }
