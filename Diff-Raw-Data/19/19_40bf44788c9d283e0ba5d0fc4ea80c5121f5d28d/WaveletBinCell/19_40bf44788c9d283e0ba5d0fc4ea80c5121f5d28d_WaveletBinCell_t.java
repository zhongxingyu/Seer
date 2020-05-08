 package binpack.gwa;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.syncleus.dann.genetics.AbstractValueGene;
 import com.syncleus.dann.genetics.wavelets.AbstractWaveletGene;
 import com.syncleus.dann.genetics.wavelets.Cell;
 import com.syncleus.dann.genetics.wavelets.Nucleus;
 import com.syncleus.dann.genetics.wavelets.SignalKey;
 import com.syncleus.dann.genetics.wavelets.SignalKeyConcentration;
 
 public class WaveletBinCell extends Cell{
	public WaveletBinCell(Nucleus nucleus)
 	{
		super(nucleus);
 		System.out.println("cell");
 		this.localConcentrations = new HashSet<SignalKeyConcentration>();
		
 		Set<SignalKey> localSignals = this.nucleus.getExpressedSignals(false);
 		for(SignalKey localSignal : localSignals)
 		{
 			SignalKeyConcentration newConcentration = new SignalKeyConcentration(localSignal);
 			this.localConcentrations.add(newConcentration);
 			this.nucleus.bind(newConcentration, false);
 		}
 	}
	
 	public WaveletBinCell mate(WaveletBinCell cell){
 		return null;//return nucleus.mate(cell.nucleus);
 	}
 	public List<AbstractWaveletGene> getGenes(){
 		return ((WaveletBinNucleus)nucleus).getGenes();
 	}
 	public Set<AbstractWaveletGene> getOrderedGenes(){
 		TreeSet<AbstractWaveletGene> sortedGenes = new TreeSet<AbstractWaveletGene>(new Comparator<AbstractWaveletGene>()
 				{
 					public int compare(AbstractWaveletGene gene1, AbstractWaveletGene gene2)
 					{
 						if( gene1.expressionActivity() < gene2.expressionActivity() )
 							return -1;
 						else if( gene1.expressionActivity() > gene2.expressionActivity() )
 							return 1;
 						else
 							return 0;
 					}
 				});
 
 			sortedGenes.addAll(this.getGenes());
 
 			return Collections.unmodifiableSortedSet(sortedGenes);
 	}
 }
