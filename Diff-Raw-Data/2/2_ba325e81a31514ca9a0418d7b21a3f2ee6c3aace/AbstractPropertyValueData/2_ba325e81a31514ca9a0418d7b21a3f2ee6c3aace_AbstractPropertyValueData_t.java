 package org.selfbus.sbtools.knxcom.application;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 /**
  * Abstract base class for property value write and response classes.
  */
 public abstract class AbstractPropertyValueData extends AbstractPropertyValue
 {
    private byte[] data;
 
    /**
     * Create a property-value-data object.
     */
    protected AbstractPropertyValueData()
    {
       super();
    }
    
    /**
     * Create a property-value-data object.
     * 
     * @param objectId - the ID of the property object.
     * @param propertyId - the ID of the property in the property object.
     * @param startIndex - the first element to access.
     * @param count - the number of elements to access.
     */
    protected AbstractPropertyValueData(int objectId, int propertyId, int startIndex, int count)
    {
       super(objectId, propertyId, startIndex, count);
    }
 
    /**
     * @return the data
     */
    public byte[] getData()
    {
       return data;
    }
 
    /**
     * @param data the data to set
     */
    public void setData(byte[] data)
    {
       this.data = data;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public void readData(DataInput in, int length) throws IOException
    {
       super.readData(in, length);
 
       if (count > 0)
       {
         data = new byte[length - 4];
          in.readFully(data);
       }
       else data = null;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public void writeData(DataOutput out) throws IOException
    {
       super.writeData(out);
 
       if (data != null)
          out.write(data);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
       final StringBuffer sb = new StringBuffer();
       sb.append(super.toString());
 
       if (data != null)
       {
          sb.append(':');
          for (int i = 0; i < data.length; ++i)
             sb.append(String.format(" %02X", data[i] & 255));
       }
 
       return sb.toString();
    }
 }
