 package binpack.gwa;
 
 import java.util.HashSet;
 
 import com.syncleus.dann.genetics.wavelets.AbstractOrganism;
 import com.syncleus.dann.genetics.wavelets.Cell;
 
 public class WaveletBinIndividual extends AbstractOrganism{
 	private WaveletBinCell cell;
 	public WaveletBinIndividual(){
 		System.out.println("individual");
		cell = new WaveletBinCell(new WaveletBinNucleus());
 		System.out.println("individual wtf");
 	}
 	public WaveletBinIndividual(WaveletBinCell c){
 		cell = c;
 	}
 	@Override
 	public AbstractOrganism mate(AbstractOrganism partner) {
 		if (partner instanceof WaveletBinIndividual)
 			return new WaveletBinIndividual(((WaveletBinIndividual)partner).getCell().mate(cell));
 		return null;
 	}
 	public WaveletBinCell getCell(){
 		return (WaveletBinCell)cell;
 	}
 	public void tick(){
 		for (Cell c : cells)
 			c.tick();
 	}
 
 }
