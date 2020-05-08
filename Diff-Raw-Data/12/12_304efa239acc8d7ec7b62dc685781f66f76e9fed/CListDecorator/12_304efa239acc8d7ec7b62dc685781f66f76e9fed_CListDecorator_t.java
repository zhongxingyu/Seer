 package calico.components.decorators;
 
 import it.unimi.dsi.fastutil.longs.Long2ReferenceArrayMap;
 
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.apache.commons.lang.ArrayUtils;
 
 import calico.COptions;
 import calico.CalicoServer;
 import calico.controllers.CGroupController;
 import calico.controllers.CGroupDecoratorController;
 import calico.controllers.CStrokeController;
 import calico.networking.netstuff.CalicoPacket;
 import calico.networking.netstuff.NetworkCommand;
 
 public class CListDecorator extends CGroupDecorator {
 	
 	int iconWidth = 16, iconHeight = 16, iconWidthBuffer = 4;
 	int iconXSpace = this.iconWidth + this.iconWidthBuffer*2;
 	int widthBuffer = 5;
 	
 	private final boolean debugListOrder = true;
 	
 	static
 	{
 		CGroupDecoratorController.registerGroupDecoratorCommand("LIST");
 	}
 	
 	public CListDecorator(long guuid, long uuid, long cuuid, long puuid) {
 		super(guuid, uuid, cuuid, puuid);
 		initializeCheckValues();
 	}
 
 	public CListDecorator(long guuid, long uuid)
 	{
 		super(guuid, uuid);
 		initializeCheckValues();
 //		if (getDecoratedGroup() != null)
 //			resetListElementPositions(true);
 	}
 	
 	@Override
 	protected void setNetworkCommand() {
 		this.networkLoadCommand = NetworkCommand.LIST_LOAD;
 	}
 	
 	@Override
 	public void setChildGroups(long[] gplist) {
 		super.setChildGroups(gplist);
 //		CGroupDecoratorController.groupCheckValues.clear();
 		initializeCheckValues();
 //		resetListElementPositions();
 	}
 	
 	@Override
 	public void addChildGroup(long grpUUID, int x, int y) {
 //		System.out.println("Before add child:");
 //		printBounds();
 		if (CGroupController.exists(grpUUID) && !CGroupController.groups.get(grpUUID).isPermanent())
 			return;
 		
 		super.addChildGroup(grpUUID, x, y);
 		if (!CGroupDecoratorController.groupCheckValues.containsKey(grpUUID))
 			CGroupDecoratorController.groupCheckValues.put(grpUUID, new Boolean(false));
 		
 		resetListElementPositions(grpUUID, x, y);
 		
 		recomputeBounds();
 		recomputeValues();
 //		CalicoServer.logger.debug("Group added to list: " + grpUUID);
 //		System.out.println("After add child:");
 //		printBounds();
 	}
 	
 	@Override
 	public void deleteChildGroup(long grpUUID) {
 		super.deleteChildGroup(grpUUID);
 
 		resetListElementPositions();
 		
 		recomputeBounds();
 		recomputeValues();
 //		CalicoServer.logger.debug("Group removed from list: " + grpUUID);
 	}
 	
 	public void setCheck(long guuid, boolean value)
 	{
 		CGroupDecoratorController.groupCheckValues.put(guuid, new Boolean(value));
 	}
 	
 	@Override
 	public boolean containsShape(Shape shape)
 	{
 //		printBounds();
 		System.out.println("Shape: " + shape.getBounds2D().getCenterX() + ", " + shape.getBounds2D().getCenterY());
 		return this.containsPoint((int)shape.getBounds2D().getCenterX(), (int)shape.getBounds2D().getCenterY());
 	}
 	
 	@Override
 	public void recomputeBounds()
 	{
 		if (getDecoratedGroup() != null && getDecoratedGroup().getPathReference() != null)
 		{
 			resetListElementPositions();
 			
 			Rectangle bounds = getDecoratedGroup().getBoundsOfContents();
 			
 			Rectangle newBounds = new Rectangle(bounds.x - widthBuffer - iconXSpace, bounds.y,
 					bounds.width + widthBuffer*2 + iconXSpace, bounds.height);
 			
 			CGroupController.no_notify_make_rectangle(getDecoratedUUID(), newBounds.x, newBounds.y, newBounds.width, newBounds.height);
 			
 			
 //			this.invalidatePaint();
 //			this.repaint();
 //			getDecoratedGroup().repaint();
 		}
 		super.recomputeBounds();
 	}
 	
 	@Override
 	public void recomputeValues()
 	{
 		if (getDecoratedGroup() == null)
 			return;
 		
 		long[] childGroups = getDecoratedGroup().getChildGroups();
 		for (int i = 0; i < childGroups.length; i++)
 		{
 			if (CGroupController.groups.get(childGroups[i]) instanceof CListDecorator)
 			{
 				CListDecorator innerList = ((CListDecorator)CGroupController.groups.get(childGroups[i]));
 				if (innerList.getChildGroups().length > 0)
 				{
 					boolean containsUnchecked = false;
 					long[] innerListGroups = innerList.getChildGroups();
 					for (int j = 0; j < innerListGroups.length; j++)
 						if (CGroupDecoratorController.groupCheckValues.get(innerListGroups[j]).booleanValue() == false)
 							containsUnchecked = true;
 					
 					
 					if (containsUnchecked)
 //						CGroupDecoratorController.groupCheckValues.put(childGroups[i], new Boolean(false));
 						setCheck(childGroups[i], false);
 					else
 //						CGroupDecoratorController.groupCheckValues.put(childGroups[i], new Boolean(true));
 						setCheck(childGroups[i], true);
 				}
 			}
 		}
 		super.recomputeValues();
 	}
 	
 	public void resetListElementPositions()
 	{
 		resetListElementPositions(false);
 	}
 	
 	public void resetListElementPositions(boolean setLocationToFirstElement)
 	{
 		resetListElementPositions(setLocationToFirstElement, 0l, 0, 0);
 	}
 	
 	public void resetListElementPositions(long guuid, int g_x, int g_y)
 	{
 		resetListElementPositions(false, guuid, g_x, g_y);
 	}
 	
 	public void resetListElementPositions(boolean setLocationToFirstElement, long guuid, int g_x, int g_y) {
 		
 		int moveToX, moveToY, deltaX, deltaY, elementSpacing = 5;
 		
 		
 		int yOffset = /*elementSpacing +*/ 0;
 		int widestWidth = 0;
 		int x, y;
 		
 		if (setLocationToFirstElement && getChildGroups().length > 0)
 		{
 			long firstchild = getChildGroups()[0];
 			x = CGroupController.groups.get(firstchild).getPathReference().getBounds().x - widthBuffer - iconXSpace; //  bounds.x; // + widthBuffer / 2;
 			y = CGroupController.groups.get(firstchild).getPathReference().getBounds().y - yOffset; //bounds.y; // + elementSpacing / 2;
 		}
 		else 
 		{
 			if (getPathReference() == null)
 				return;
 			x = getPathReference().getBounds().getBounds().x + COptions.group.padding;
 			y = getPathReference().getBounds().getBounds().y + COptions.group.padding;
 		}
 		
 		Rectangle bounds; // = getDecoratedGroup().getPathReference().getBounds();
 		
 		long[] listElements = getChildGroups();
 		
 		listElements = insertGroupByYPosition(listElements, guuid, g_x, g_y);
 		
 		if (listElements.length < 1 || CGroupController.groups.get(listElements[0]) == null || CGroupController.groups.get(listElements[0]).getPathReference() == null)
 			return;
 		
 		if (debugListOrder)
 		{
 			Rectangle lb = getPathReference().getBounds().getBounds();
 			System.out.printf("List (%d)/\n\t bounds: (%d,%d,%d,%d)", this.uuid, lb.x, lb.y, lb.width, lb.height);
 			System.out.println("");
 		}
 		for (int i = 0; i < listElements.length; i++)
 		{
 			if (!CGroupController.exists(listElements[i]))
 				continue;
 			
 			//destination
 			moveToX = x + widthBuffer + iconXSpace;
 			moveToY = y;
 			
 			//figure out offset
 			bounds = CGroupController.groups.get(listElements[i]).getPathReference().getBounds();
 			deltaX = moveToX - bounds.x;
 			deltaY = moveToY - bounds.y;
 			
 			if (debugListOrder)
 			{
 				System.out.printf("\t%d: %d, %d, %d, %d", i, moveToX, moveToY + yOffset, bounds.width, bounds.height);
 				System.out.println("");
 			}
 			
 			CGroupController.no_notify_move(listElements[i], deltaX, deltaY + yOffset);
 			yOffset += bounds.height + elementSpacing;
 			
 			//check for the widest width
 			if (bounds.width > widestWidth)
 				widestWidth = bounds.width;
 		}
 		if (listElements.length == 0)
 		{
 			widestWidth = 100;
 			yOffset = 50;
 		}
 	}
 	
 	/**
 	 * This method assumes that the given array is already ordered by their Y position
 	 * 
 	 * @param listElements
 	 * @param guuid
 	 * @param gX
 	 * @param gY
 	 * @return
 	 */
 	private long[] insertGroupByYPosition(long[] listElements, long guuid,
 			int gX, int gY) {
 		
 		if (!CGroupController.exists(guuid))
 			return listElements;
 		
 		long[] retList = listElements.clone(); 
 		
 		for (int i = 0; i < retList.length; i++)
 			if (retList[i] == guuid)
 				retList = ArrayUtils.removeElement(retList, guuid);
 				
 
 		for (int i = 0; i < retList.length; i++)
 		{
 			if (CGroupController.groups.get(retList[i]).getMidPoint().getY() > gY)
 			{
 				retList = ArrayUtils.add(retList, i, guuid);
 				break;
 			}
 		}
 		
 		if (retList.length > 0)
 		{
 			if (CGroupController.groups.get(retList[retList.length-1]).getMidPoint().getY() < gY)
 				retList = ArrayUtils.add(retList, guuid);
 		}
 		
 		return retList;
 	}
 	
 	private long[] orderByYAxis(long[] listItems)
 	{
 		if (getDecoratedGroup() == null)
 			return null;
 		
 		int[] yValues = new int[listItems.length];
 		
 		//copy Y values to array to sort
 		for (int i=0;i<listItems.length;i++)
 		{
 			yValues[i] = (int)CGroupController.groups.get(listItems[i]).getMidPoint().getY();
 		}
 		
 		//sort the y values
 		java.util.Arrays.sort(yValues);
 		
 		//match the y values back to their Groups and return the sorted array
 		long[] sortedElementList = new long[listItems.length];
 		for (int i=0;i<listItems.length;i++)
 		{
 			for (int j=0;j<listItems.length;j++)
 			{
				if (CGroupController.exists(listItems[j]))
 				{
					if ((int)CGroupController.groups.get(listItems[j]).getMidPoint().getY() == yValues[i])
					{
						sortedElementList[i] = listItems[j];
						//This line needed in case multiple groups have the same y value
						listItems[j] = -1;
						break;
					}
 				}
 			}
 		}
 		
 		return sortedElementList;
 	}
 	
 	private void initializeCheckValues()
 	{
 		if (getDecoratedGroup() == null)
 			return;
 		
 		long[] childGroups = getDecoratedGroup().getChildGroups().clone();
 		for (int i = 0; i < childGroups.length; i++)
 		{
 			if (!CGroupDecoratorController.groupCheckValues.containsKey(childGroups[i]))
 			{
 				CGroupDecoratorController.groupCheckValues.put(childGroups[i], new Boolean(false));
 			}
 		}
 	}
 	
 	@Override
 	public CalicoPacket[] getDecoratorUpdatePackets(long uuid, long cuid, long puid, long decorated_uuid) {
 		if (getDecoratedGroup() == null)
 			return new CalicoPacket[] { };
 		CalicoPacket[] packets = new CalicoPacket[getChildGroups().length + 1];
 		packets[0] = CalicoPacket.getPacket(NetworkCommand.LIST_LOAD, this.uuid, this.cuid, this.puid, getDecoratedGroup().getUUID());
 		long[] keySet = getChildGroups();
 		for (int i = 0; i < keySet.length; i++)
 		{
 			packets[i+1] = CalicoPacket.getPacket(NetworkCommand.LIST_CHECK_SET, this.uuid, this.cuid, this.puid, keySet[i], CGroupDecoratorController.groupCheckValues.get(keySet[i]).booleanValue());
 		}
 		return packets;
 	}
 	
 	@Override
 	public CalicoPacket[] getDecoratorUpdatePackets(long uuid, long cuid, long puid, long decorated_uuid, Long2ReferenceArrayMap<Long> subGroupMappings) {
 		CalicoPacket[] superPackets = super.getDecoratorUpdatePackets(uuid, cuid, puid, decorated_uuid, subGroupMappings);
 		long[] keySet = subGroupMappings.keySet().toLongArray();
 		
 		CalicoPacket[] packets = new CalicoPacket[keySet.length + superPackets.length];
 		
 		for (int i = 0; i < superPackets.length; i++)
 			packets[i] = superPackets[i];
 		
 		
 		for (int i = 0; i < keySet.length; i++)
 		{
 			boolean checkValue = false;
 			long subGroupMapping = 0l;
 			if (CGroupDecoratorController.groupCheckValues.get(keySet[i]) != null)
 				checkValue = CGroupDecoratorController.groupCheckValues.get(keySet[i]).booleanValue();
 			if (subGroupMappings.get(keySet[i]) != null)
 				subGroupMapping = subGroupMappings.get(keySet[i]).longValue();
 			
 			packets[i+superPackets.length] = CalicoPacket.getPacket(NetworkCommand.LIST_CHECK_SET, uuid, cuid, puid, subGroupMapping, checkValue);
 		}
 		return packets;
 	}
 	
 	@Override
 	public boolean canParentChild(long child, int x, int y)
 	{
 		if (child == 0l || child == this.uuid)
 			return false;
 		
 		if (CGroupController.exists(getParentUUID())
 				&& CGroupController.groups.get(getParentUUID()) instanceof CGroupDecorator)
 			return false;
 		
 		long potentialParent_new_parent = 0l;
 		long child_parent = 0l;
 		
 		potentialParent_new_parent = getParentUUID();
 		
 		if (CStrokeController.strokes.containsKey(child))
 		{
 			return false;
 		}
 		else if (CGroupController.groups.containsKey(child))
 		{
 			if (!CGroupController.groups.get(child).isPermanent())
 				return false;
 			
 			//must contain center of mass
 			Point2D center = CGroupController.groups.get(child).getMidPoint();
 			if (!this.containsPoint(x, y))
 				return false;
 			
 			child_parent = CGroupController.groups.get(child).getParentUUID();
 		}
 		
 		if (CGroupController.group_is_ancestor_of(child, this.uuid))
 			return false;
 		
 		if (child_parent == 0l)
 			return true;
 		
 		return potentialParent_new_parent == child_parent;
 	}
 	
 	@Override
 	public void rotate(double radians) 
 	{
 		//Do nothing
 	}
 	
 	@Override
 	public void rotate(double radians, Point2D pivotPoint) 
 	{
 		//Do nothing	
 	}
 	
 	@Override
 	public void scale(double scaleX, double scaleY)
 	{
 		//Do nothing
 	}
 	
 	@Override
 	public void scale(double scaleX, double scaleY, Point2D pivotPoint)
 	{
 		//Do nothing
 	}
 	
 	@Override
 	public void delete() {
 		super.delete();
 	}
 	
 	@Override
 	public long[] getChildGroups()
 	{
 		return orderByYAxis(super.getChildGroups());
 	}
 	
 }
