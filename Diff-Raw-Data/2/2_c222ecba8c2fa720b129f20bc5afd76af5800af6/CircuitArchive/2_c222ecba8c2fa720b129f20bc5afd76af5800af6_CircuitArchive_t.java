 package aeroport.sgbag.xml;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import lombok.AllArgsConstructor;
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;
 
 import org.eclipse.swt.graphics.Point;
 
 import aeroport.sgbag.kernel.ElementCircuit;
 import aeroport.sgbag.kernel.Noeud;
 import aeroport.sgbag.kernel.Rail;
 import aeroport.sgbag.utils.CircuitGenerator;
 import aeroport.sgbag.views.VueChariot;
 import aeroport.sgbag.views.VueHall;
 import aeroport.sgbag.views.VueRail;
 import aeroport.sgbag.views.VueTapisRoulant;
 import aeroport.sgbag.views.VueToboggan;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 import com.thoughtworks.xstream.mapper.CannotResolveClassException;
 
 @NoArgsConstructor
 @AllArgsConstructor
 @XStreamAlias("circuit")
 public class CircuitArchive {
 	
 	// Description de la structure de notre fichier XML 
 	@Data
 	@EqualsAndHashCode
 	@XStreamAlias("elementCircuit")
 	public static class ElementCircuitSaved {
 		@XStreamOmitField
 		protected ElementCircuit unpackedObject = null;
 	}
 	
 	@Data
 	@EqualsAndHashCode(callSuper=true)
 	@AllArgsConstructor
 	@XStreamAlias("rail")
 	public static class RailSaved extends ElementCircuitSaved {
 		@XStreamAsAttribute
 		private NoeudSaved from;
 		@XStreamAsAttribute
 		private NoeudSaved to;
 	}
 	
 	@Data
 	@EqualsAndHashCode(callSuper=true)
 	@AllArgsConstructor
 	@NoArgsConstructor
 	@XStreamAlias("noeud")
 	public static class NoeudSaved extends ElementCircuitSaved {
 		@XStreamAsAttribute
 		protected int x;
 		@XStreamAsAttribute
 		protected int y;
 	}
 	
 	@Data
 	@EqualsAndHashCode(callSuper=true)
 	@AllArgsConstructor
 	@XStreamAlias("toboggan")
 	public static class TobogganSaved extends NoeudSaved {
 		@XStreamAsAttribute
 		private int angle;
 		
 		public TobogganSaved(int x, int y, int angle) {
 			this.x = x;
 			this.y = y;
 			this.angle = angle;
 		}
 	}
 	
 	@Data
 	@EqualsAndHashCode(callSuper=true)
 	@AllArgsConstructor
 	@XStreamAlias("tapis")
 	public static class TapisRoulantSaved extends NoeudSaved {
 		@XStreamAsAttribute
 		private int length;
 		
 		@XStreamAsAttribute
 		private int vitesse;
 		
 		@XStreamAsAttribute
 		private int distanceEntreBagage;
 		
 		@XStreamAsAttribute
 		private int angle;
 		
 		@XStreamAsAttribute
 		private Boolean autoGeneration = false;
 		
 		public TapisRoulantSaved(int x, int y, int length, int vitesse,
 				int distanceEntreBagage, int angle, Boolean autoGeneration) {
 			super(x, y);
 			this.length = length;
 			this.vitesse = vitesse;
 			this.distanceEntreBagage = distanceEntreBagage;
 			this.angle = angle;
 			this.autoGeneration = autoGeneration;
 		}
 	}
 	
 	@Data
 	@XStreamAlias("chariot")
 	@AllArgsConstructor
 	public static class ChariotSaved {
 		final public static int DEFAULT_WIDTH = 20;
 		final public static int DEFAULT_SPEED = 20;
 		
 		@XStreamAsAttribute
 		private ElementCircuitSaved on;
 		
 		@XStreamAsAttribute
 		private int position = 0;
 		
 		@XStreamAsAttribute
 		private int maxMoveDistance;
 		
 		@XStreamAsAttribute
 		private NoeudSaved to;
 		
 		@XStreamAsAttribute
 		private int length = DEFAULT_WIDTH;
 		
 		@XStreamAsAttribute
 		private int maxSpeed = DEFAULT_SPEED;
 	}
 	
 	@Getter
 	@Setter
 	private LinkedList<TobogganSaved> toboggans = new LinkedList<CircuitArchive.TobogganSaved>();
 	
 	@Getter
 	@Setter
 	private LinkedList<TapisRoulantSaved> tapisRoulants = new LinkedList<CircuitArchive.TapisRoulantSaved>();
 	
 	@Getter
 	@Setter
 	private LinkedList<NoeudSaved> noeuds = new LinkedList<CircuitArchive.NoeudSaved>();
 	
 	@Getter
 	@Setter
 	private LinkedList<RailSaved> rails = new LinkedList<CircuitArchive.RailSaved>();
 	
 	@Getter
 	@Setter
 	private LinkedList<ChariotSaved> chariots = new LinkedList<CircuitArchive.ChariotSaved>();
 	
 	/**
 	 * Extrait le contenu de la sauvegarde du circuit dans un nouveau
 	 * Hall.
 	 * @param vueHall
 	 */
 	public void unpackTo(VueHall vueHall) {
 		CircuitGenerator cg = new CircuitGenerator(vueHall);
 		
 		if(rails != null){
 			for(RailSaved railp: rails){
 				VueRail vr = cg.createSegment(new Point(railp.from.x, railp.from.y), 
 							                  new Point(railp.to.x, railp.to.y));
 				
 				railp.getFrom().setUnpackedObject(vr.getRail().getNoeudPrecedent());
 				railp.getTo().setUnpackedObject(vr.getRail().getNoeudSuivant());
 				railp.setUnpackedObject(vr.getRail());
 			}
 		}
 		
 		if(tapisRoulants != null){
 			for(TapisRoulantSaved tapisRoulant: tapisRoulants){
 				VueTapisRoulant vtr = cg.createEntry(new Point(tapisRoulant.x, tapisRoulant.y),
 													 tapisRoulant.length, tapisRoulant.vitesse,
 													 tapisRoulant.distanceEntreBagage, 
 													 tapisRoulant.autoGeneration);
 				
 				vtr.setAngle(tapisRoulant.angle);
 				vtr.updateView();
 				
 				tapisRoulant.setUnpackedObject((ElementCircuit) vtr.getTapisRoulant().getConnexionCircuit());
 			}
 		}
 		
 		if(toboggans != null){
 			for(TobogganSaved tobogganp: toboggans){
 				VueToboggan vt = cg.createExit(new Point(tobogganp.x, tobogganp.y));
 				
 				vt.setAngle(tobogganp.angle);
 				vt.updateView();
 				
 				tobogganp.setUnpackedObject((ElementCircuit) vt.getToboggan().getConnexionCircuit());
 			}
 		}
 		
 		if(chariots != null){
 			for(ChariotSaved chariotp: chariots){
 				if(chariotp.on instanceof RailSaved){
 					VueChariot vc = cg.addChariot((Rail) chariotp.on.unpackedObject, chariotp.maxSpeed,
 											      chariotp.length, chariotp.position, (Noeud) chariotp.to.unpackedObject, 
 											      null, null);
 					vc.updateView();
 				}else if(chariotp.on instanceof NoeudSaved){
 					VueChariot vc = cg.addChariot((Noeud) chariotp.on.unpackedObject, chariotp.maxSpeed,
 												  chariotp.length, (Noeud) chariotp.to.unpackedObject, null, null);
 					vc.updateView();
 				}
 			}
 		}
		
		vueHall.getHall().init();
 	}
 	
 	/**
 	 * Méthode utilitaire permettant de créer rapidement un CircuitArchive depuis
 	 * un fichier.	
 	 * @throws {@link IOException} 
 	 * @throws {@link MalformedCircuitArchiveException}
 	 */
 	public static CircuitArchive readFromXML(String path) throws MalformedCircuitArchiveException, 
 																 FileNotFoundException,
 															     IOException {
 		XStream xStream = new XStream();
 		
 		xStream.processAnnotations(CircuitArchive.class);
 	
 		xStream.setMode(XStream.ID_REFERENCES);
 		
 		String xmlContent = "";
 		
 		FileReader freader = new FileReader(path);
 		BufferedReader in = new BufferedReader(freader);
 		String tmp = "";
 	
 		while ((tmp = in.readLine()) != null) {
 			xmlContent += (tmp + '\n');
 		}
 		
 		CircuitArchive ca = null;
 		
 		try {
 			ca = (CircuitArchive) xStream.fromXML(xmlContent);
 		} catch (CannotResolveClassException e){
 			throw new MalformedCircuitArchiveException();
 		}
 		
 		if(ca == null){
 			throw new MalformedCircuitArchiveException();
 		}
 		
 		return ca;
 	}
 }
