 package petrinetze.impl;
 
 import petrinetze.IRenew;
 
 public class RenewCount implements IRenew {
	private String tlb;
 	@Override
 	public String renew(String tlb) {
 		
		int c = Integer.parseInt(tlb);
		c++;
		this.tlb = String.valueOf(c);
		return this.tlb;
 	}
 
 	@Override
 	public boolean isTlbValid(String tlb) {
		try {
			Integer.parseInt(tlb);
			return true;
		} catch (NumberFormatException exc) {
			return false;
		}
 	}
 	
 	/**
 	 * @return Den Namen der Transition.
 	 */
 	public String getTlb() {
		return this.tlb;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
		result = prime * result + ((tlb == null) ? 0 : tlb.hashCode());
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
		if (tlb == null) {
			if (other.tlb != null)
				return false;
		} else if (!tlb.equals(other.tlb))
			return false;
		return true;
 	}
 	
 
 }
