 package petrinetze.impl;
 
 import petrinetze.IRenew;
 
 public class RenewCount implements IRenew {
	private int count = 0;
 	@Override
 	public String renew(String tlb) {
 		
		count++;
		return "" + count;
 	}
 
 	@Override
 	public boolean isTlbValid(String tlb) {
		return true;
 	}
 	
 	/**
 	 * @return Den Namen der Transition.
 	 */
 	public String getTlb() {
		return "" + count;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
		result = prime * result + count;
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		RenewCount other = (RenewCount) obj;
		return getTlb().equals(other.getTlb());
 	}
 	
 
 }
