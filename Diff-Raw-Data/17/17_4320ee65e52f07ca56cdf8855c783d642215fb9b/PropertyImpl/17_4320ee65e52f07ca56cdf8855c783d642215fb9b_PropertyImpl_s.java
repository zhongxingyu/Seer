 package ch.x42.terye;
 
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.Calendar;
 
 import javax.jcr.Binary;
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.Node;
 import javax.jcr.Property;
 import javax.jcr.RepositoryException;
 import javax.jcr.Value;
 import javax.jcr.ValueFormatException;
 import javax.jcr.lock.LockException;
 import javax.jcr.nodetype.ConstraintViolationException;
 import javax.jcr.nodetype.PropertyDefinition;
 import javax.jcr.version.VersionException;
 
 public class PropertyImpl extends ItemImpl implements Property {
 
     private Value value;
 
     public PropertyImpl(SessionImpl session, Path path, Value value) {
         // TODO: validate name
         super(session, path);
         this.value = value;
     }
 
     public PropertyImpl(SessionImpl session, PropertyImpl property) {
         super(session, property.path);
         // XXX: copy state
         this.value = property.value;
     }
 
     @Override
     public Binary getBinary() throws ValueFormatException, RepositoryException {
        // TODO Auto-generated method stub
        return null;
     }
 
     @Override
     public boolean getBoolean() throws ValueFormatException,
             RepositoryException {
         return value.getBoolean();
     }
 
     @Override
     public Calendar getDate() throws ValueFormatException, RepositoryException {
         return value.getDate();
     }
 
     @Override
     public BigDecimal getDecimal() throws ValueFormatException,
             RepositoryException {
         return value.getDecimal();
     }
 
     @Override
     public PropertyDefinition getDefinition() throws RepositoryException {
         return new PropertyDefinitionImpl();
     }
 
     @Override
     public double getDouble() throws ValueFormatException, RepositoryException {
         return value.getDouble();
     }
 
     @Override
     public long getLength() throws ValueFormatException, RepositoryException {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public long[] getLengths() throws ValueFormatException, RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public long getLong() throws ValueFormatException, RepositoryException {
         return value.getLong();
     }
 
     @Override
     public Node getNode() throws ItemNotFoundException, ValueFormatException,
             RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public Property getProperty() throws ItemNotFoundException,
             ValueFormatException, RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public InputStream getStream() throws ValueFormatException,
             RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public String getString() throws ValueFormatException, RepositoryException {
         return value.getString();
     }
 
     @Override
     public int getType() throws RepositoryException {
         return value.getType();
     }
 
     @Override
     public Value getValue() throws ValueFormatException, RepositoryException {
         return value;
     }
 
     @Override
     public Value[] getValues() throws ValueFormatException, RepositoryException {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public boolean isMultiple() throws RepositoryException {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public void setValue(BigDecimal value) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void setValue(Binary value) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void setValue(boolean value) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         setValue(getSession().getValueFactory().createValue(value));
     }
 
     @Override
     public void setValue(Calendar value) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         setValue(getSession().getValueFactory().createValue(value));
     }
 
     @Override
     public void setValue(double value) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         setValue(getSession().getValueFactory().createValue(value));
     }
 
     @Override
     public void setValue(InputStream value) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void setValue(long value) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         setValue(getSession().getValueFactory().createValue(value));
     }
 
     @Override
     public void setValue(Node value) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void setValue(String value) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         setValue(getSession().getValueFactory().createValue(value));
     }
 
     @Override
     public void setValue(String[] values) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void setValue(Value value) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         this.value = value;
     }
 
     @Override
     public void setValue(Value[] values) throws ValueFormatException,
             VersionException, LockException, ConstraintViolationException,
             RepositoryException {
         // TODO Auto-generated method stub
 
     }
 
 }
