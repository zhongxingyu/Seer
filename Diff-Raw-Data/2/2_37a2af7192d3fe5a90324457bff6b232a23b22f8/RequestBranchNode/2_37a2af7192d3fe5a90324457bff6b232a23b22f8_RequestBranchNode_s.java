 package mrtjp.projectred.transportation;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import mrtjp.projectred.core.utils.HashPair2;
 import mrtjp.projectred.core.utils.ItemKey;
 import mrtjp.projectred.core.utils.ItemKeyStack;
 import mrtjp.projectred.core.utils.Pair2;
 import mrtjp.projectred.transportation.RequestBranch.RequestFlags;
 import mrtjp.projectred.transportation.Router.StartEndPath;
 
 public class RequestBranchNode 
 {
     public RequestBranchNode(CraftingPromise parentCrafter, ItemKeyStack requestedPackage, IWorldRequester requester, RequestBranchNode parent, EnumSet<RequestFlags> type) {
         this.requestedPackage = requestedPackage;
         this.requester = requester;
         this.parent = parent;
 
         if (parent != null) {
             parent.subRequests.add(this);
             root = parent.root;
         } else
             root = (RequestBranch) this;
 
         if (parentCrafter != null)
             if (!recurse_IsCrafterUsed(parentCrafter))
                 usedCrafters.add(parentCrafter);
 
         if (type.contains(RequestFlags.PULL) && getPromisesFromBroadcasters())
             return;
 
         if (type.contains(RequestFlags.CRAFT) && getPromisesFromExcess())
             return;
 
         if (type.contains(RequestFlags.CRAFT) && getPromisesFromCrafters())
             return;
     }
 
     private final ItemKeyStack requestedPackage;
     private final IWorldRequester requester;
     private final RequestBranchNode parent;
     protected final RequestBranch root;
 
     private List<RequestBranchNode> subRequests = new ArrayList<RequestBranchNode>();
     private List<DeliveryPromise> promises = new ArrayList<DeliveryPromise>();
     private List<ExcessPromise> excessPromises = new ArrayList<ExcessPromise>();
     private SortedSet<CraftingPromise> usedCrafters = new TreeSet<CraftingPromise>();
 
     private CraftingPromise parityBranch = null;
 
     private int promisedCount = 0;
 
     public int getPromisedCount() {
         return promisedCount;
     }
 
     public int getMissingCount() {
         return requestedPackage.stackSize - promisedCount;
     }
 
     public ItemKey getRequestedPackage() {
         return requestedPackage.key();
     }
 
     public boolean isDone() {
         return getMissingCount() <= 0;
     }
 
     public void addPromise(DeliveryPromise promise) {
         if (!promise.thePackage.equals(getRequestedPackage()))
             return;
 
         if (promise.size > getMissingCount()) {
             int more = promise.size - getMissingCount();
             promise.size = getMissingCount();
 
             ExcessPromise excess = new ExcessPromise();
             excess.setPackage(promise.thePackage).setSize(more).setSender(promise.sender);
             excessPromises.add(excess);
         }
 
         if (promise.size <= 0)
             return;
 
         promises.add(promise);
         promisedCount += promise.size;
         root.promiseAdded(promise);
     }
 
     private boolean getPromisesFromCrafters() {
         List<StartEndPath> allRouters = new ArrayList<StartEndPath>(requester.getRouter().getRoutesByCost());
         Collections.sort(allRouters, new PathSorter(0));
         
         List<CraftingPromise> allCrafters = new ArrayList<CraftingPromise>(allRouters.size());
         for (StartEndPath l : allRouters) {
             Router r = l.end;
 
             if (r.getParent() instanceof IWorldCrafter) {
                 IWorldCrafter cr = (IWorldCrafter)r.getParent();
                 CraftingPromise cp = cr.requestCraftPromise(getRequestedPackage());
                 if (cp != null)
                     allCrafters.add(cp);
             }
         }
 
         Iterator<CraftingPromise> allCraftersIt = allCrafters.iterator();
 
         PriorityQueue<CraftingTreeInteraction> balanced = new PriorityQueue<CraftingTreeInteraction>(5);
         ArrayList<CraftingTreeInteraction> unbalanced = new ArrayList<CraftingTreeInteraction>();
 
         boolean recursionFinished = false;
 
         CraftingPromise lastCrafter = null;
         int currentPriority = 0;
 
         while (!recursionFinished) {
             if (allCraftersIt.hasNext()) {
                 if (lastCrafter == null)
                     lastCrafter = allCraftersIt.next();
             } else if (lastCrafter == null)
                 recursionFinished = true;
 
             int itemsNeeded = getMissingCount();
 
             if (lastCrafter != null && (balanced.isEmpty() || currentPriority == lastCrafter.getPriority())) {
                 currentPriority = lastCrafter.getPriority();
                 CraftingPromise crafter = lastCrafter;
                 lastCrafter = null;
                 if (recurse_IsCrafterUsed(crafter))
                     continue;
 
                 CraftingTreeInteraction cti = new CraftingTreeInteraction(crafter, itemsNeeded, this);
                 balanced.add(cti);
                 continue;
             }
 
             if (unbalanced.isEmpty() && balanced.isEmpty())
                 continue;
 
             if (balanced.size() == 1) {
                 unbalanced.add(balanced.poll());
                 unbalanced.get(0).addAdditionalItems(itemsNeeded);
             } else {
                 if (!balanced.isEmpty())
                     unbalanced.add(balanced.poll());
 
                 while (!unbalanced.isEmpty() && itemsNeeded > 0) {
 
                     while (!balanced.isEmpty() && balanced.peek().toDo() <= unbalanced.get(0).toDo())
                         unbalanced.add(balanced.poll());
 
                     int cap;
                     if (!balanced.isEmpty())
                         cap = balanced.peek().toDo();
                     else
                         cap = Integer.MAX_VALUE;
 
                     int floor = unbalanced.get(0).toDo();
                     cap = Math.min(cap, floor + (itemsNeeded + unbalanced.size() - 1) / unbalanced.size());
 
                     Iterator<CraftingTreeInteraction> iter = unbalanced.iterator();
                     while (iter.hasNext()) {
                         CraftingTreeInteraction crafter = iter.next();
                         int request = Math.min(itemsNeeded, cap - floor);
                         if (request > 0) {
                             int craftingDone = crafter.addAdditionalItems(request);
                             itemsNeeded -= craftingDone;
                         }
                     }
                 }
             }
 
             Iterator<CraftingTreeInteraction> iter = unbalanced.iterator();
             while (iter.hasNext()) {
                 CraftingTreeInteraction c = iter.next();
                 if (c.setsRequested > 0 && !c.finalizeInteraction())
                     iter.remove();
             }
             itemsNeeded = getMissingCount();
 
             if (itemsNeeded <= 0)
                 break;
 
             if (!unbalanced.isEmpty())
                 recursionFinished = false;
         }
         return isDone();
     }
 
     private boolean getPromisesFromExcess() {
         LinkedList<ExcessPromise> availableExcess = root.getAllExcessFor(getRequestedPackage());
         for (ExcessPromise excess : availableExcess) {
             if (isDone())
                 break;
 
             if (excess.size <= 0)
                 continue;
 
             excess.size = Math.min(excess.size, getMissingCount());
             addPromise(excess);
         }
 
         return isDone();
     }
 
     private boolean getPromisesFromBroadcasters() {
         List<StartEndPath> allRouters = new ArrayList<StartEndPath>(requester.getRouter().getRoutesByCost());
         Collections.sort(allRouters, new PathSorter(1.0));
 
         for (StartEndPath l : allRouters) {
             if (isDone())
                 break;
             Router r = l.end;
             IWorldRouter member = r.getParent();
 
            if (member.needsWork() || !(member instanceof IWorldBroadcaster))
                 continue;
             
             if (LogisticPathFinder.sharesInventory(requester.getContainer(), member.getContainer()))
                 continue;
             
             IWorldBroadcaster member2 = (IWorldBroadcaster) member;
             member2.requestPromises(this, root.getExistingPromisesFor(new HashPair2<IWorldBroadcaster, ItemKey>(member2, getRequestedPackage())));
         }
         return isDone();
     }
 
     private int getPotentialSubPromises(int numberOfSets, CraftingPromise crafter) {
         boolean failed = false;
         int potentialSets = numberOfSets;
 
         List<Pair2<ItemKeyStack, IWorldRequester>> ingredients = crafter.getScaledIngredients(numberOfSets);
         ArrayList<RequestBranchNode> children = new ArrayList<RequestBranchNode>(ingredients.size());
 
         for (Pair2<ItemKeyStack, IWorldRequester> item : ingredients) {
             RequestBranchNode req = new RequestBranchNode(crafter, item.getValue1(), item.getValue2(), this, RequestFlags.def);
             children.add(req);
             if (!req.isDone())
                 failed = true;
         }
 
         if (failed) {
             for (RequestBranchNode sub : children)
                 sub.destroy();
 
             parityBranch = crafter;
 
             for (int i = 0; i < ingredients.size(); i++)
                 potentialSets = Math.min(potentialSets, children.get(i).getPromisedCount() / (ingredients.get(i).getValue1().stackSize / numberOfSets));
 
             return getAbsoluteSubPromises(potentialSets, crafter);
         }
 
         return potentialSets;
     }
 
     private int getAbsoluteSubPromises(int numberOfSets, CraftingPromise crafter) {
         ArrayList<RequestBranchNode> children = new ArrayList<RequestBranchNode>();
         if (numberOfSets > 0) {
             List<Pair2<ItemKeyStack, IWorldRequester>> ingredients = crafter.getScaledIngredients(numberOfSets);
             boolean failed = false;
 
             for (Pair2<ItemKeyStack, IWorldRequester> item : ingredients) {
                 RequestBranchNode req = new RequestBranchNode(crafter, item.getValue1(), item.getValue2(), this, RequestFlags.def);
                 children.add(req);
                 if (!req.isDone())
                     failed = true;
             }
 
             if (failed) {
                 for (RequestBranchNode sub : children)
                     sub.destroy();
                 return 0;
             }
         }
         return numberOfSets;
     }
 
     private void destroy() {
         parent.remove(this);
     }
 
     protected void remove(RequestBranchNode subNode) {
         subRequests.remove(subNode);
         subNode.recurse_RemoveSubPromisses();
     }
 
     protected void recurse_RemoveSubPromisses() {
         for(DeliveryPromise promise:promises)
             root.promiseRemoved(promise);
 
         for(RequestBranchNode subNode:subRequests)
             subNode.recurse_RemoveSubPromisses();
     }
 
     protected boolean recurse_IsCrafterUsed(CraftingPromise parentCrafter) {
         if (!usedCrafters.isEmpty() && usedCrafters.contains(parentCrafter))
             return true;
         if (parent == null)
             return false;
         return parent.recurse_IsCrafterUsed(parentCrafter);
     }
 
     protected void recurse_RequestDelivery() {
         for (RequestBranchNode subReq : subRequests)
             subReq.recurse_RequestDelivery();
 
         for (DeliveryPromise p : promises)
             p.sender.deliverPromises(p, requester);
 
         for (ExcessPromise p : excessPromises)
             if (p.sender instanceof IWorldCrafter)
                 ((IWorldCrafter)p.sender).registerExcess(p);
 
     }
 
     protected void recurse_GatherExcess(ItemKey item, HashMap<IWorldBroadcaster, List<ExcessPromise>> excessMap) {
         for (ExcessPromise extra : excessPromises)
             if (extra.thePackage == item) {
                 List<ExcessPromise> extras = excessMap.get(extra.sender);
                 if (extras == null) {
                     extras = new LinkedList<ExcessPromise>();
                     excessMap.put(extra.sender, extras);
                 }
                 extras.add(extra.copy());
             }
 
         for (RequestBranchNode subNode : subRequests)
             subNode.recurse_GatherExcess(item, excessMap);
     }
 
     protected void recurse_RemoveUnusableExcess(ItemKey item, HashMap<IWorldBroadcaster, List<ExcessPromise>> excessMap) {
         for (DeliveryPromise promise : promises) {
             if (promise.thePackage != item)
                 continue;
 
             if (!(promise instanceof ExcessPromise))
                 continue;
 
             ExcessPromise epromise = (ExcessPromise) promise;
 
             if (epromise.used)
                 continue;
 
             int usedcount = epromise.size;
 
             List<ExcessPromise> extras = excessMap.get(epromise.sender);
             if (extras == null)
                 continue;
 
             Iterator<ExcessPromise> it = extras.iterator();
             while (it.hasNext()) {
                 ExcessPromise extra = it.next();
                 if (extra.size >= usedcount) {
                     extra.size -= usedcount;
                     usedcount = 0;
                     break;
                 } else {
                     usedcount -= extra.size;
                     it.remove();
                 }
             }
         }
 
         for (RequestBranchNode subNode : subRequests)
             subNode.recurse_RemoveUnusableExcess(item, excessMap);
     }
 
     protected void recurse_RebuildParityTree() {
         if (isDone())
             return;
 
         if (parityBranch == null)
             return;
 
         int setsNeeded = (getMissingCount() + parityBranch.getSizeForSet() - 1) / parityBranch.getSizeForSet();
 
         List<Pair2<ItemKeyStack, IWorldRequester>> components = parityBranch.getScaledIngredients(setsNeeded);
 
         for (Pair2<ItemKeyStack, IWorldRequester> pair : components)
             new RequestBranchNode(parityBranch, pair.getValue1(), pair.getValue2(), this, RequestFlags.def);
 
         addPromise(parityBranch.getScaledPromise(setsNeeded));
 
         for (RequestBranchNode sub : subRequests)
             sub.recurse_RebuildParityTree();
     }
 
     protected void recurse_GatherStatisticsMissing(Map<ItemKey, Integer> map) {
         int missing = getMissingCount();
         if (missing > 0) {
             ItemKey item = getRequestedPackage();
             Integer current = map.get(item);
             if (current == null)
                 current = 0;
             current += missing;
             map.put(item, current);
         }
 
         for (RequestBranchNode sub : subRequests)
             sub.recurse_GatherStatisticsMissing(map);
     }
 
     protected void recurse_GatherStatisticsUsed(Map<ItemKey, Integer> map) {
         int thisUsed = 0;
         for (DeliveryPromise p : promises)
             if (!(p.sender instanceof IWorldCrafter))
                 thisUsed += p.size;
 
         if (thisUsed > 0) {
             ItemKey item = getRequestedPackage();
             Integer current = map.get(item);
             if (current == null)
                 current = 0;
             current += thisUsed;
             map.put(item, current);
         }
 
         for (RequestBranchNode sub : subRequests)
             sub.recurse_GatherStatisticsUsed(map);
     }
 
     public static class DeliveryPromise 
     {
         public ItemKey thePackage;
         public int size;
         public IWorldBroadcaster sender;
 
         public DeliveryPromise setPackage(ItemKey thePackage) {
             this.thePackage = thePackage;
             return this;
         }
         public DeliveryPromise setSize(int size) {
             this.size = size;
             return this;
         }
         public DeliveryPromise setSender(IWorldBroadcaster sender) {
             this.sender = sender;
             return this;
         }
 
         public DeliveryPromise copy() {
             DeliveryPromise p = new DeliveryPromise();
             p.setPackage(thePackage).setSize(size).setSender(sender);
             return p;
         }
     }
     public static class ExcessPromise extends DeliveryPromise 
     {
         public boolean used;
 
         public ExcessPromise setUsed(boolean flag) {
             used = flag;
             return this;
         }
 
         @Override
         public ExcessPromise copy() {
             ExcessPromise p = new ExcessPromise();
             p.setPackage(thePackage).setSize(size).setSender(sender);
             p.setUsed(used);
             return p;
         }
     }
     public static class CraftingPromise implements Comparable<CraftingPromise> 
     {
         private IWorldCrafter crafter;
 
         private ItemKeyStack result;
         private ArrayList<Pair2<ItemKeyStack, IWorldRequester>> ingredients = new ArrayList<Pair2<ItemKeyStack, IWorldRequester>>(9);
 
         private final int priority;
 
         public CraftingPromise(ItemKeyStack result, IWorldCrafter crafter, int priority) {
             this.result = result;
             this.crafter = crafter;
             this.priority = priority;
         }
 
         public IWorldCrafter getCrafter() {
             return crafter;
         }
 
         public int getPriority() {
             return priority;
         }
 
         public CraftingPromise addIngredient(ItemKeyStack ingredient, IWorldRequester crafter) {
             for (Pair2<ItemKeyStack, IWorldRequester> ing : ingredients)
                 if (ing.getValue1().key().equals(ingredient.key()) && ing.getValue2() == crafter) {
                     ing.getValue1().stackSize += ingredient.stackSize;
                     return this;
                 }
             ingredients.add(new Pair2<ItemKeyStack, IWorldRequester>(ingredient, crafter));
             return this;
         }
 
         public DeliveryPromise getScaledPromise(int numberOfSets) {
             DeliveryPromise p = new DeliveryPromise();
             p.setPackage(result.key().copy()).setSize(result.stackSize * numberOfSets).setSender(crafter);
             return p;
         }
 
         public List<Pair2<ItemKeyStack, IWorldRequester>> getScaledIngredients(int numberOfSets) {
             List<Pair2<ItemKeyStack, IWorldRequester>> components = new ArrayList<Pair2<ItemKeyStack, IWorldRequester>>(ingredients.size());
 
             for (Pair2<ItemKeyStack, IWorldRequester> ing : ingredients) {
                 Pair2<ItemKeyStack, IWorldRequester> newIng = new Pair2<ItemKeyStack, IWorldRequester>(ing.getValue1().copy(), ing.getValue2());
                 newIng.getValue1().stackSize *= numberOfSets;
                 components.add(newIng);
             }
 
             return components;
         }
 
         @Override
         public int compareTo(CraftingPromise p2) {
             int c = priority - p2.priority;
             if (c == 0)
                 c = result.compareTo(p2.result);
             if (c == 0)
                 c = crafter.getRouter().compareTo(p2.crafter.getRouter());
             return c;
         }
 
         public boolean canCraft(ItemKey item) {
             return item.equals(result);
         }
 
         public int getSizeForSet() {
             return result.stackSize;
         }
 
         public ItemKey getResultItem() {
             return result.key();
         }
     }
 
     private class CraftingTreeInteraction implements Comparable<CraftingTreeInteraction> 
     {
         private int setsRequested;
         private final int setSize;
         private final int maxSetsAvailable;
         private final RequestBranchNode treeNode;
 
         public final CraftingPromise crafter;
         public final int originalToDo;
 
         private CraftingTreeInteraction(CraftingPromise crafter, int maxToCraft, RequestBranchNode interaction) {
             this.crafter = crafter;
             this.treeNode = interaction;
             this.originalToDo = crafter.getCrafter().itemsToProcess();
             this.setsRequested = 0;
             this.setSize = crafter.getSizeForSet();
             this.maxSetsAvailable = (treeNode.getMissingCount() + setSize - 1) / setSize;
         }
 
         public int toDo() {
             return originalToDo + setsRequested * setSize;
         }
 
         private int calculateMaxPotentialSets(int maxSets) {
             int needed = 0;
             if (maxSets > 0)
                 needed = maxSets;
             else
                 needed = (treeNode.getMissingCount() + setSize - 1) / setSize;
 
             if (needed <= 0)
                 return 0;
 
             return getPotentialSubPromises(needed, crafter);
         }
 
         public int addAdditionalItems(int additional) {
             int stacksRequested = (additional + setSize - 1) / setSize;
             setsRequested += stacksRequested;
             return stacksRequested*setSize;
         }
 
         public boolean finalizeInteraction() {
             int setsToCraft = Math.min(setsRequested, maxSetsAvailable);
             int setsAbleToCraft = calculateMaxPotentialSets(setsToCraft);
 
             if (setsAbleToCraft > 0) {
                 DeliveryPromise delivery = crafter.getScaledPromise(setsAbleToCraft);
                 if (delivery.size != setsAbleToCraft*setSize)
                     return false;
                 treeNode.addPromise(delivery);
             }
 
             boolean isDone = setsToCraft == setsAbleToCraft;
             setsRequested = 0;
 
             return isDone;
         }
 
         @Override
         public int compareTo(CraftingTreeInteraction o2) {
             return toDo() - o2.toDo();
         }
     }
     
     private static class PathSorter implements Comparator<StartEndPath> 
     {
         private final double distanceWeight;
         public PathSorter(double distanceWeight) {
             this.distanceWeight = distanceWeight;
         }
         
         @Override
         public int compare(StartEndPath o1, StartEndPath o2) {
             double c = 0;
             
             IWorldRouter wr1 = o1.end.getParent();
             IWorldRouter wr2 = o2.end.getParent();
             
             int p1 = wr1 instanceof IWorldBroadcaster ? ((IWorldBroadcaster)wr1).getPriority() : Integer.MIN_VALUE;
             int p2 = wr2 instanceof IWorldBroadcaster ? ((IWorldBroadcaster)wr2).getPriority() : Integer.MIN_VALUE;
             
             if (p1 > Integer.MIN_VALUE) {
                 if (p2 > Integer.MIN_VALUE)
                     c = p2 - p1;
                 else
                     return -1;
             } else if (p2 > Integer.MIN_VALUE)
                 return 1;
 
             if (c != 0)
                 return (int) c;
 
             int switchKey = 1;
             if (o1.end.getIPAddress() - o2.end.getIPAddress() > 0) {
                 switchKey = -1;
                 StartEndPath temp = o1;
                 o1 = o2;
                 o2 = temp;
             }
             
             double l1 = wr1 instanceof IWorldBroadcaster ? ((IWorldBroadcaster)wr1).getWorkLoad() : 0;
             double l2 = wr2 instanceof IWorldBroadcaster ? ((IWorldBroadcaster)wr2).getWorkLoad() : 0;
             
             c = l1 - l2;
             c += (o1.distance - o2.distance) * distanceWeight;
             
             if (c == 0)
                 return -switchKey;
             
             if (c > 0)
                 return (int) (c + 0.5) * switchKey;
             else
                 return (int) (c - 0.5) * switchKey;
         }
     }
 }
