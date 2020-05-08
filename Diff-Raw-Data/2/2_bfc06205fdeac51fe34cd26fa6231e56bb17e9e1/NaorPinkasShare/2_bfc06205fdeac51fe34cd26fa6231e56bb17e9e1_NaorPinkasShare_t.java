package cryptocast.crypto.naorpinkas;
 
 import cryptocast.crypto.*;
 import java.math.BigInteger;
 
 /**
  * A share in the Naor-Pinkas broadcast encryption scheme. It consists of a tuple
  * $(I, g^{r P(I))}$. $t$ distinct shares of this form are sufficient to restore the
  * value $g^{r P(0)}$, where $t$ is the degree of the polynomial $P$.
  */
 public class NaorPinkasShare implements Share<BigInteger> {
     /** {@inheritDoc} */
     public boolean isComplete() { return false; }
     /** {@inheritDoc} */
     public byte[] pack() { return null; }
     /** {@inheritDoc} */
     public BigInteger restore() throws InsufficientInformationException { return null; }
 }
 
