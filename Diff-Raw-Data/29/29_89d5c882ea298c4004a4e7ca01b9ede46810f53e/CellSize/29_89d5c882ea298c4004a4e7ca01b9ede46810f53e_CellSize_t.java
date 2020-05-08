 package swarm.shared.structs;
 
 import swarm.shared.json.A_JsonFactory;
 import swarm.shared.json.E_JsonKey;
 import swarm.shared.json.I_JsonArray;
 import swarm.shared.json.I_JsonObject;
 import swarm.shared.json.I_ReadsJson;
 import swarm.shared.json.I_WritesJson;
 
 public class CellSize implements I_ReadsJson, I_WritesJson
 {
	protected static final int INVALID_DIMENSION = -2;
 	protected static final int DEFAULT_DIMENSION = -1;
 	
 	protected int m_width = DEFAULT_DIMENSION;
 	protected int m_height = DEFAULT_DIMENSION;
 	
 	public CellSize()
 	{
 		
 	}
 	
 	public CellSize(CellSize value_copied)
 	{
 		this.copy(value_copied);
 	}
 	
 	public CellSize(I_JsonObject json, A_JsonFactory jsonFactory)
 	{
 		this.readJson(jsonFactory, json);
 	}
 	
 	public boolean isDefault()
 	{
 		return m_width == DEFAULT_DIMENSION && m_height == DEFAULT_DIMENSION;
 	}
 	
	public boolean isInvalid()
	{
		return m_width == INVALID_DIMENSION && m_height == INVALID_DIMENSION;
	}
	
	public void setToInvalid()
	{
		m_width = m_height = INVALID_DIMENSION;
	}
	
 	public void setToDefaults()
 	{
 		m_width = m_height = DEFAULT_DIMENSION;
 	}
 	
 	public void setIfDefault(int width, int height)
 	{
 		m_width = m_width == DEFAULT_DIMENSION ? width : m_width;
 		m_height = m_height == DEFAULT_DIMENSION ? height : m_height;
 	}
 	
 	public void copy(CellSize value)
 	{
 		m_width = value.m_width;
 		m_height = value.m_height;
 	}
 	
 	public void copyIfDefault(CellSize value)
 	{
 		m_width = m_width == DEFAULT_DIMENSION ? value.m_width : m_width;
 		m_height = m_height == DEFAULT_DIMENSION ? value.m_height : m_height;
 	}
 	
 	@Override
 	public void readJson(A_JsonFactory factory, I_JsonObject json)
 	{
 		I_JsonArray sizeArray = factory.getHelper().getJsonArray(json, E_JsonKey.cellSize);
 		
 		if( sizeArray != null )
 		{
 			m_width = sizeArray.getInt(0);
 			m_height = sizeArray.getInt(1);
 		}
 		else
 		{
			this.setToInvalid();
 		}
 	}
 
 	@Override
 	public void writeJson(A_JsonFactory factory, I_JsonObject json_out)
 	{
 		I_JsonArray sizeArray = factory.createJsonArray();
 		
 		sizeArray.addInt(m_width);
 		sizeArray.addInt(m_height);
 	}
 }
