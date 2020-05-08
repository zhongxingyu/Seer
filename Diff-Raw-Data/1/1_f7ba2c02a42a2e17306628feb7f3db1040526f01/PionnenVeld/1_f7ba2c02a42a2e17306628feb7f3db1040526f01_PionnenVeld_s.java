 package ambiorix.gui;
 
 import java.awt.Component;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableModel;
 
 import ambiorix.spelbord.Pion;
 import ambiorix.spelbord.PionBasis;
 
 public class PionnenVeld extends JPanel implements PionLuisteraar{
 	private Vector<Pion_Gui> mijnPionnen;
 	private Vector<PionLuisteraar> mijnPionLuisteraars;
 	private HoofdVenster hv; // TODO tijdelijk!! (voor printjes te kunnen doen)
 	
 	public PionnenVeld(HoofdVenster hv) {
 		this.hv = hv;
 		mijnPionnen = new Vector<Pion_Gui>();
 		mijnPionLuisteraars = new Vector<PionLuisteraar>();
 		this.setLayout(new PionnenVeldLayout());
 	}
 	
 	public void voegPionToe(PionBasis pion)
 	{
 		Pion_Gui nieuwePion= new Pion_Gui(pion);
 		nieuwePion.setVisible(true);
 		nieuwePion.voegPionLuisteraarToe(this);
 		mijnPionnen.add(nieuwePion);
 		this.add(nieuwePion);
 		this.repaint();
		this.revalidate();
 	}
 	
 	public void verwijderPion(PionBasis pion)
 	{
 		Pion_Gui teVerwijderenPion = null;
 		Component teVerwijderenComp = null;
 		
 		for(Pion_Gui pg : mijnPionnen) {
 			if(pg.getPion() == pion)
 				teVerwijderenPion = pg;
 		}
 		
 		Component comps[] = getComponents();
 		for(Component c : comps) {
 			Pion_Gui pg = (Pion_Gui) c;
 			if(pg.getPion() == pion)
 				teVerwijderenComp = c;
 		}
 		
 		if(teVerwijderenPion != null && teVerwijderenComp != null) {
 			mijnPionnen.remove(teVerwijderenPion);
 			this.remove(teVerwijderenComp);
 			
 			this.repaint();
 			this.revalidate();
 		}
 		
 	}
 	
 	public synchronized void addPionLuisteraar(PionLuisteraar pl)
 	{
 		mijnPionLuisteraars.add(pl);
 	}
 	
 	public synchronized void removePionLuisteraar(PionLuisteraar pl)
 	{
 		
 		mijnPionLuisteraars.remove(pl);
 	}
 
 	@Override
 	public synchronized void geklikt(PionGebeurtenis pg) {
 		Iterator<PionLuisteraar> it = mijnPionLuisteraars.iterator();
 		this.hv.voegRegelToe("Op een pion geklikt");
 		while(it.hasNext()) {
 			it.next().geklikt(pg);
 		}
 	}
 
 	public void ledig() {
 		mijnPionnen.clear();
 		this.removeAll();
 		this.repaint();
 		this.revalidate();
 	}
 }
