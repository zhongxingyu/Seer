 /*
  * Copyright (c) 2004-2013 Universidade do Porto - Faculdade de Engenharia
  * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
  * All rights reserved.
  * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
  *
  * This file is part of Neptus, Command and Control Framework.
  *
  * Commercial Licence Usage
  * Licencees holding valid commercial Neptus licences may use this file
  * in accordance with the commercial licence agreement provided with the
  * Software or, alternatively, in accordance with the terms contained in a
  * written agreement between you and Universidade do Porto. For licensing
  * terms, conditions, and further information contact lsts@fe.up.pt.
  *
  * European Union Public Licence - EUPL v.1.1 Usage
  * Alternatively, this file may be used under the terms of the EUPL,
  * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
  * included in the packaging of this file. You may not use this work
  * except in compliance with the Licence. Unless required by applicable
  * law or agreed to in writing, software distributed under the Licence is
  * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
  * ANY KIND, either express or implied. See the Licence for the specific
  * language governing permissions and limitations at
  * https://www.lsts.pt/neptus/licence.
  *
  * For more information please see <http://lsts.fe.up.pt/neptus>.
  *
  * Author: pdias
  * May 19, 2013
  */
 package pt.up.fe.dceg.neptus.mp.maneuvers;
 
 import java.awt.Graphics2D;
 import java.util.Arrays;
 import java.util.Vector;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.Node;
 
 import pt.up.fe.dceg.neptus.NeptusLog;
 import pt.up.fe.dceg.neptus.gui.editor.SpeedUnitsEditor;
 import pt.up.fe.dceg.neptus.i18n.I18n;
 import pt.up.fe.dceg.neptus.imc.CompassCalibration.DIRECTION;
 import pt.up.fe.dceg.neptus.imc.IMCMessage;
 import pt.up.fe.dceg.neptus.mp.Maneuver;
 import pt.up.fe.dceg.neptus.mp.ManeuverLocation;
 import pt.up.fe.dceg.neptus.mp.SystemPositionAndAttitude;
 import pt.up.fe.dceg.neptus.plugins.NeptusProperty;
 import pt.up.fe.dceg.neptus.plugins.PluginProperty;
 import pt.up.fe.dceg.neptus.plugins.PluginUtils;
 import pt.up.fe.dceg.neptus.renderer2d.StateRenderer2D;
 import pt.up.fe.dceg.neptus.types.coord.LocationType;
 import pt.up.fe.dceg.neptus.types.map.PlanElement;
 
 import com.l2fprod.common.propertysheet.DefaultProperty;
 import com.l2fprod.common.propertysheet.Property;
 
 /**
  * @author pdias
  *
  */
 public class CompassCalibration extends Maneuver implements LocatedManeuver, IMCSerialization, StatisticsProvider {
 
     protected static final String DEFAULT_ROOT_ELEMENT = "CompassCalibration";
 
     //@NeptusProperty(name="Location")
     public ManeuverLocation location = new ManeuverLocation();
 
     @NeptusProperty(name="Speed", description="The speed to be used")
    public double speed = 1000; 
 
     @NeptusProperty(name="Speed units", description="The speed units", editorClass=SpeedUnitsEditor.class)
    public String speedUnits = "RPM";
 
     @NeptusProperty(name="Pitch", description="The Pitch angle used to perform the maneuver.")
     public double pitchDegs = 15;
 
     @NeptusProperty(name="Amplitude", description="Yoyo motion amplitude.")
    public double amplitude = 10;
 
     @NeptusProperty(name="Duration (s)", description="The duration in seconds of this maneuver. Use '0' for unlimited duration time.")
    public int duration = 180;
 
     @NeptusProperty(name="Radius (m)", description="Radius of the maneuver.")
     public float radius = 5;
 
     @NeptusProperty(name="Direction", description="Direction of the maneuver.")
     public pt.up.fe.dceg.neptus.imc.CompassCalibration.DIRECTION direction = DIRECTION.CLOCKW;
 
     
     public CompassCalibration() {
     }
 
     @Override
     public Document getManeuverAsDocument(String rootElementName) {
         Document document = DocumentHelper.createDocument();
         Element root = document.addElement( rootElementName );
         //        root.addAttribute("kind", "automatic");
         Element finalPoint = root.addElement("finalPoint");
         finalPoint.addAttribute("type", "pointType");
         Element point = getManeuverLocation().asElement("point");
         finalPoint.add(point);
 
         //Element radTolerance = finalPoint.addElement("radiusTolerance");
         //radTolerance.setText("0");
 
         Element pitchEl = root.addElement("pitch");
         pitchEl.setText(String.valueOf(pitchDegs));
 
         Element amplitudeEl = root.addElement("amplitude");
         amplitudeEl.setText(String.valueOf(amplitude));
 
         Element durationEl = root.addElement("duration");
         durationEl.setText(String.valueOf(duration));
 
         Element speedEl = root.addElement("speed");
         //speedEl.addAttribute("tolerance", String.valueOf(speedTolerance));
         speedEl.addAttribute("type", "float");
         speedEl.addAttribute("unit", speedUnits);
         speedEl.setText(String.valueOf(speed));
 
         Element radiusEl = root.addElement("radius");
         radiusEl.setText(String.valueOf(radius));
 
         Element directionEl = root.addElement("direction");
         directionEl.setText(String.valueOf(direction.value()));
 
         return document;
     }
 
     /* (non-Javadoc)
      * @see pt.up.fe.dceg.neptus.mp.Maneuver#loadFromXML(java.lang.String)
      */
     @Override
     public void loadFromXML(String xml) {
         try {
             Document doc = DocumentHelper.parseText(xml);
             Node node = doc.selectSingleNode(DEFAULT_ROOT_ELEMENT+ "/finalPoint/point");
             if (node == null)
                 node = doc.selectSingleNode(DEFAULT_ROOT_ELEMENT+ "/initialPoint/point"); // to read old elevator specs
             ManeuverLocation loc = new ManeuverLocation();
             loc.load(node.asXML());
             setManeuverLocation(loc);
 
             Node pitchNode = doc.selectSingleNode(DEFAULT_ROOT_ELEMENT+ "/pitch");
             pitchDegs = Double.parseDouble(pitchNode.getText());
 
             Node amplitudeNode = doc.selectSingleNode(DEFAULT_ROOT_ELEMENT+ "/amplitude");
             amplitude = Double.parseDouble(amplitudeNode.getText());
 
             Node durationNode = doc.selectSingleNode(DEFAULT_ROOT_ELEMENT+ "/duration");
             duration = Integer.parseInt(durationNode.getText());
             
             Node speedNode = doc.selectSingleNode(DEFAULT_ROOT_ELEMENT+ "/speed");
             speed = Double.parseDouble(speedNode.getText());
             speedUnits = speedNode.valueOf("@unit");
 
             radius = Float.parseFloat(doc.selectSingleNode(DEFAULT_ROOT_ELEMENT+ "/radius").getText());
 
         }
         catch (Exception e) {
             NeptusLog.pub().error(this, e);
             return;
         }
     }
 
     
     /* (non-Javadoc)
      * @see pt.up.fe.dceg.neptus.mp.Maneuver#ManeuverFunction(pt.up.fe.dceg.neptus.mp.VehicleState)
      */
     @Override
     public SystemPositionAndAttitude ManeuverFunction(SystemPositionAndAttitude lastVehicleState) {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public ManeuverLocation getManeuverLocation() {
         return location.clone();
     }
 
     @Override
     public ManeuverLocation getEndLocation() {
         return getManeuverLocation();
     }
 
     @Override
     public ManeuverLocation getStartLocation() {
         return getManeuverLocation();
     }
 
     @Override
     public void setManeuverLocation(ManeuverLocation loc) {
         location = loc.clone();
     }
 
     @Override
     public void translate(double offsetNorth, double offsetEast, double offsetDown) {
         getManeuverLocation().translatePosition(offsetNorth, offsetEast, offsetDown);
     }
 
     @Override
     public String getTooltipText() {
         return super.getTooltipText() + "<hr>" + "speed: <b>" + speed + " " + speedUnits + "</b>" + 
                 ("<br>cruise depth: <b>" + (int) getStartLocation().getDepth() + " m</b>") + 
                 "<br>end z: <b>" + getManeuverLocation().getZ() + " m (" + I18n.text(getManeuverLocation().getZUnits().toString()) + ")</b>" +
                 "<br>pitch: <b>" + pitchDegs + " m</b>" +                
                 "<br>amplitude: <b>" + amplitude + " m</b>" +                
                 "<br>radius: <b>" + radius + " m</b>" +                
                 "<br>duration: <b>" + duration + " m</b>";                
     }
 
     public String validatePitchDegs(double value) {
         NeptusLog.pub().info("<###>validate...");
         if (value < 0 || value > (float)45)
             return "Pitch angle shoud be bounded between [0\u00B0, 45\u00B0]";
         return null;
     }
 
     @Override
     protected Vector<DefaultProperty> additionalProperties() {
         Vector<DefaultProperty> properties = new Vector<DefaultProperty>();
         PluginProperty[] prop = PluginUtils.getPluginProperties(this);
         properties.addAll(Arrays.asList(prop));
         return properties;
     }
 
     @Override
     public void setProperties(Property[] properties) {
         super.setProperties(properties);
         PluginUtils.setPluginProperties(this, properties);
     }
 
     @Override
     public IMCMessage serializeToIMC() {
         getManeuverLocation().convertToAbsoluteLatLonDepth();
 
         pt.up.fe.dceg.neptus.imc.CompassCalibration man = new pt.up.fe.dceg.neptus.imc.CompassCalibration();
 
         man.setTimeout(getMaxTime());
         man.setLat(getManeuverLocation().getLatitudeAsDoubleValueRads());
         man.setLon(getManeuverLocation().getLongitudeAsDoubleValueRads());
         man.setZ(getManeuverLocation().getZ());
         man.setZUnits(getManeuverLocation().getZUnits().toString());
         man.setPitch(Math.toRadians(pitchDegs));
         man.setAmplitude(amplitude);
         man.setDuration(duration);
         man.setRadius(radius);
         man.setSpeed(speed);
         man.setDirection(direction);
         man.setCustom(getCustomSettings());
         
         switch (speedUnits) {
             case "m/s":
                 man.setSpeedUnits(pt.up.fe.dceg.neptus.imc.CompassCalibration.SPEED_UNITS.METERS_PS);
                 break;
             case "RPM":
                 man.setSpeedUnits(pt.up.fe.dceg.neptus.imc.CompassCalibration.SPEED_UNITS.RPM);
                 break;
             default:
                 man.setSpeedUnits(pt.up.fe.dceg.neptus.imc.CompassCalibration.SPEED_UNITS.PERCENTAGE);
                 break;
         }
 
         return man;
     }
     @Override
     public void parseIMCMessage(IMCMessage message) {
         if (!DEFAULT_ROOT_ELEMENT.equalsIgnoreCase(message.getAbbrev()))
             return;
         pt.up.fe.dceg.neptus.imc.CompassCalibration man = null;
         try {
              man = pt.up.fe.dceg.neptus.imc.CompassCalibration.clone(message);
         }
         catch (Exception e) {
             e.printStackTrace();
             return;
         }
         
         setMaxTime(man.getTimeout());
         ManeuverLocation loc = new ManeuverLocation();
         loc.setLatitude(Math.toDegrees(man.getLat()));
         loc.setLongitude(Math.toDegrees(man.getLon()));
         loc.setZ(man.getZ());
         NeptusLog.pub().info("<###> "+man.getZUnits());
 //        loc.setZUnits(pt.up.fe.dceg.neptus.mp.ManeuverLocation.Z_UNITS.valueOf(elev.getEndZUnits().toString()));
         loc.setZUnits(ManeuverLocation.Z_UNITS.valueOf(message.getString("end_z_units").toString()));
         setManeuverLocation(loc);
         pitchDegs = Math.toDegrees(man.getPitch());
         amplitude = man.getAmplitude();
         duration = man.getDuration();
         radius = (float) man.getRadius();
         speed = man.getSpeed();
         
         direction = man.getDirection();
 
         setCustomSettings(man.getCustom());
         
         switch (man.getSpeedUnits()) {
             case RPM:
                 speedUnits = "RPM";
                 break;
             case METERS_PS:
                 speedUnits = "m/s";
                 break;
             case PERCENTAGE:
                 speedUnits = "%";
                 break;
             default:
                 break;
         }
     }
 
 //    setTimeout(timeout);
 //    setLat(lat);
 //    setLon(lon);
 //    setZ(z);
 //    setZUnits(z_units);
 //    setPitch(pitch);
 //    setAmplitude(amplitude);
 //    setDuration(duration);
 //    setSpeed(speed);
 //    setSpeedUnits(speed_units);
 //    setRadius(radius);
 //    setDirection(direction);
 
     @Override
     public Object clone() {
         CompassCalibration clone = new CompassCalibration();
         super.clone(clone);
         clone.setManeuverLocation(getManeuverLocation());
         clone.pitchDegs = pitchDegs;
         clone.amplitude = amplitude;
         clone.duration = duration;
         clone.radius = radius;
         clone.speed = speed;
         clone.speedUnits = speedUnits;
         clone.direction = direction;
         return clone;
     }
 
     
     @Override
     public void paintOnMap(Graphics2D g2d, PlanElement planElement, StateRenderer2D renderer) {
         super.paintOnMap(g2d, planElement, renderer);
         
         LocationType man_loc = this.getStartLocation();
         Graphics2D g = (Graphics2D)g2d.create();
         g.translate(-renderer.getScreenPosition(man_loc).getX(), -renderer.getScreenPosition(man_loc).getY());
 
         g.dispose();
     }
 
     
     @Override
     public double getCompletionTime(LocationType initialPosition) {
         double speed = this.speed;
         if (this.speedUnits.equalsIgnoreCase("RPM")) {
             speed = speed/769.230769231; //1.3 m/s for 1000 RPMs
         }
         else if (this.speedUnits.equalsIgnoreCase("%")) {
             speed = speed/76.923076923; //1.3 m/s for 100% speed
         }
       
         return getDistanceTravelled(initialPosition) / speed;
     }
 
     @Override
     public double getDistanceTravelled(LocationType initialPosition) {
         double meters = getStartLocation().getDistanceInMeters(initialPosition);
         double depthDiff = initialPosition.getAllZ();
         meters += depthDiff;
         return meters;
     }
 
     @Override
     public double getMaxDepth() {
         return getManeuverLocation().getAllZ();
     }
 
     @Override
     public double getMinDepth() {
         return getManeuverLocation().getAllZ();
     }   
 
     public static void main(String[] args) {
         CompassCalibration compc = new CompassCalibration();
         String ccmanXML = compc.getManeuverAsDocument("CompassCalibration").asXML();
         System.out.println(ccmanXML);
         CompassCalibration compc1 = new CompassCalibration();
         compc1.loadFromXML(ccmanXML);
         ccmanXML = compc.getManeuverAsDocument("CompassCalibration").asXML();
         System.out.println(ccmanXML);
         
     }
 }
