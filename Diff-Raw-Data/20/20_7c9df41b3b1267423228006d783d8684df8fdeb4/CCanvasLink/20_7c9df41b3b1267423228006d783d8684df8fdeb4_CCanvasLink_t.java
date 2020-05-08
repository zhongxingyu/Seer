 package calico.plugins.iip;
 
 import calico.networking.netstuff.CalicoPacket;
 
 public class CCanvasLink
 {
 	public enum LinkType
 	{
 		NEW_IDEA,
 		NEW_PERSPECTIVE,
 		NEW_ALTERNATIVE,
 		DESIGN_INSIDE;
 	}
 
 	private long uuid;
 
 	private LinkType linkType;
 
 	private CCanvasLinkAnchor anchorA;
 	private CCanvasLinkAnchor anchorB;
 
 	public CCanvasLink(long uuid, LinkType linkType, CCanvasLinkAnchor anchorA, CCanvasLinkAnchor anchorB)
 	{
 		this.uuid = uuid;
 		this.linkType = linkType;
 		this.anchorA = anchorA;
 		this.anchorB = anchorB;
 	}
 	
 	public long getId()
 	{
 		return uuid;
 	}
 
 	public CCanvasLinkAnchor getAnchorA()
 	{
 		return anchorA;
 	}
 	
 	public CCanvasLinkAnchor getAnchorB()
 	{
 		return anchorB;
 	}
 	
 	public void setLinkType(LinkType linkType)
 	{
 		this.linkType = linkType;
 	}
 
 	public CalicoPacket getState()
 	{
 		return CalicoPacket.getPacket(
 				IntentionalInterfacesNetworkCommands.CLINK_CREATE,
 				uuid,
 				linkType.ordinal(),
 				anchorA.getId(),
				anchorA.getCanvasId(),
				anchorA.getGroupId(),
 				anchorA.getType().ordinal(),
 				anchorA.getPoint().x,
 				anchorA.getPoint().y,
 				anchorB.getId(),
				anchorB.getCanvasId(),
				anchorB.getGroupId(),
 				anchorB.getType().ordinal(),
 				anchorB.getPoint().x,
 				anchorB.getPoint().y
 		);
 	}
 }
