 package businessLayer;
 
 import java.util.Iterator;
import java.util.SortedMap;
 
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 
 @Entity
 public class SimpleAgreementShareTableFiller extends AgreementShareTableFiller {
 
 	@ElementCollection
	private SortedMap<Float, Float> atheneumCapitalBalanceRateTable;
 	private float structuresRate;
 	private float atheneumCommonBalanceRate;
 	
 	public SimpleAgreementShareTableFiller() {
 		super();
 	}
 
	public SimpleAgreementShareTableFiller(SortedMap<Float, Float> atheneumCapitalBalanceRateTable, float structuresRate, float atheneumCommonBalanceRate) {
 		super();
 		this.atheneumCapitalBalanceRateTable = atheneumCapitalBalanceRateTable;
 		this.structuresRate = structuresRate;
 		this.atheneumCommonBalanceRate = atheneumCommonBalanceRate;
 	}
 
 	@Override
 	public void fill(AgreementShareTable table) {
		System.out.println("Filler map: " + atheneumCapitalBalanceRateTable);
		
 		float personnel = table.getPersonnel();
 
 		float athCommBal = atheneumCommonBalanceRate;
 		float struct = structuresRate;
 
 		boolean found = false;
 		float athCapBal = 0.0F;
 		Iterator<Float> it = atheneumCapitalBalanceRateTable.keySet().iterator();
 		System.out.println("Quota personale nel build: " + personnel);
 		while (false == found && it.hasNext()) {
 			float threshold = it.next();
 			System.out.println("Soglia: " + threshold);
 			if (personnel <= threshold) {
 				athCapBal = atheneumCapitalBalanceRateTable.get(threshold);
 				found = true;
 			}
 		}
 		if (false == found) {
 			throw new IllegalStateException("Atheneum rate table is not valid");
 		}
 
 		table.setAtheneumCommonBalance(athCommBal);
 		table.setStructures(struct);
 		table.setAtheneumCapitalBalance(athCapBal);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime
 				* result
 				+ ((atheneumCapitalBalanceRateTable == null) ? 0
 						: atheneumCapitalBalanceRateTable.hashCode());
 		result = prime * result
 				+ Float.floatToIntBits(atheneumCommonBalanceRate);
 		result = prime * result + Float.floatToIntBits(structuresRate);
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		SimpleAgreementShareTableFiller other = (SimpleAgreementShareTableFiller) obj;
 		if (atheneumCapitalBalanceRateTable == null) {
 			if (other.atheneumCapitalBalanceRateTable != null)
 				return false;
 		} else if (!atheneumCapitalBalanceRateTable
 				.equals(other.atheneumCapitalBalanceRateTable))
 			return false;
 		if (Float.floatToIntBits(atheneumCommonBalanceRate) != Float
 				.floatToIntBits(other.atheneumCommonBalanceRate))
 			return false;
 		if (Float.floatToIntBits(structuresRate) != Float
 				.floatToIntBits(other.structuresRate))
 			return false;
 		return true;
 	}
 	
 	
 
 }
