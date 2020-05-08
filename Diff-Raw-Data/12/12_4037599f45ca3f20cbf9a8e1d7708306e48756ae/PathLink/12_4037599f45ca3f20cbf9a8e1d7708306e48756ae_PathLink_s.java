 
 package edu.wustl.cab2b.client.ui.dag;
 
 import java.util.List;
 
 import org.netbeans.graph.api.model.builtin.GraphLink;
 
 import edu.wustl.common.querysuite.metadata.path.IPath;
 
 public class PathLink extends GraphLink
 {
 
 	private IPath m_path;
 	private List<Integer> m_associationList;
 	private int destExpressionId;
 	private int sourceExpressionId;
 
	public PathLink()
	{
		super();
	}

 	public void setPath(IPath path)
 	{
 		m_path = path;
 	}
 
 	public IPath getPath()
 	{
 		return m_path;
 	}
 
 	public List<Integer> getAssociationExpressions()
 	{
 		return m_associationList;
 	}
 
 	public void setAssociationExpressions(List<Integer> expressions)
 	{
 		m_associationList = expressions;
 	}
 
 	public void setDestinationExpressionId(int expressionId)
 	{
 		destExpressionId = expressionId;
 	}
 
 	public int getDestinationExpressionId()
 	{
 		return destExpressionId;
 	}
 
 	public void setSourceExpressionId(int expressionId)
 	{
 		sourceExpressionId = expressionId;
 	}
 
 	public int getSourceExpressionId()
 	{
 		return sourceExpressionId;
 	}
 
 	public int getLogicalConnectorExpressionId()
 	{
 		if (!m_associationList.isEmpty())
 		{
 			return m_associationList.get(0);
 		}
 		else
 		{
 			return destExpressionId;
 		}
 	}
 }
