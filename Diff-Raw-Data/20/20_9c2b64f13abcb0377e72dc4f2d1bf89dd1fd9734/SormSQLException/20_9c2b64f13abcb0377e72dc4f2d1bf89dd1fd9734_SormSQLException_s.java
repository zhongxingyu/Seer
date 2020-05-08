 package net.jonp.sorm;
 
 import java.sql.SQLException;
 
 /**
  * Wraps a {@link SQLException}. Thrown when it is not possible to throw a
  * checked exception (such as during an {@link Iterator} method).
  */
 public class SormSQLException
     extends RuntimeException
 {
     public SormSQLException(final SQLException cause)
     {
         super(cause);
     }
 
     public SormSQLException(final String message, final SQLException cause)
     {
         super(message, cause);
     }
 
     @Override
    public SQLException getCause()
     {
         return (SQLException)super.getCause();
     }
 }
