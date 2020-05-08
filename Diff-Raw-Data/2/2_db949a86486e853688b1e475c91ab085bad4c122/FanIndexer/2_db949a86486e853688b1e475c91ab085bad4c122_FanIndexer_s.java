 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.indexer;
 
 import fan.sys.Buf;
 import fan.sys.FanObj;
 import fan.sys.Pod;
 import fan.sys.Slot;
 import fan.sys.Type;
 import fanx.fcode.FConst;
 import fanx.fcode.FField;
 import fanx.fcode.FMethod;
 import fanx.fcode.FMethodVar;
 import fanx.fcode.FPod;
 import fanx.fcode.FSlot;
 import fanx.fcode.FStore;
 import fanx.fcode.FType;
 import fanx.fcode.FTypeRef;
 import java.io.File;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.regex.Pattern;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.NBFanParser;
 //import net.colar.netbeans.fan.ast.FanAstField;
 //import net.colar.netbeans.fan.ast.FanAstMethod;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase.ModifEnum;
 //import net.colar.netbeans.fan.ast.FanTypeScope;
 import net.colar.netbeans.fan.indexer.model.FanDocument;
 import net.colar.netbeans.fan.indexer.model.FanMethodParam;
 import net.colar.netbeans.fan.indexer.model.FanModelConstants;
 import net.colar.netbeans.fan.indexer.model.FanSlot;
 import net.colar.netbeans.fan.indexer.model.FanType;
 import net.colar.netbeans.fan.indexer.model.FanTypeInheritance;
 import net.colar.netbeans.fan.platform.FanPlatform;
 import net.jot.logger.JOTLoggerLocation;
 import net.jot.persistance.JOTSQLCondition;
 import net.jot.persistance.builders.JOTQueryBuilder;
 import org.netbeans.modules.parsing.api.Snapshot;
 import org.netbeans.modules.parsing.api.Source;
 import org.netbeans.modules.parsing.spi.Parser.Result;
 import org.netbeans.modules.parsing.spi.indexing.Context;
 import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
 import org.netbeans.modules.parsing.spi.indexing.Indexable;
 import org.openide.DialogDisplayer;
 import org.openide.NotifyDescriptor;
 import org.openide.filesystems.FileAttributeEvent;
 import org.openide.filesystems.FileChangeListener;
 import org.openide.filesystems.FileEvent;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileRenameEvent;
 import org.openide.filesystems.FileUtil;
 //import org.netbeans.modules.java.source.indexing.JavaBinaryIndexer;
 
 /**
  * This indexer is backed by a DB(H2 database)
  * This class does all the Index updates(write)
  * Use FanIndexQyery to search it.
  *
  * Index all the documents:
  * fan/fwt sources
  * fantom distro pods/libs
  * jdk libs ?
  * @author tcolar
  */
 public class FanIndexer extends CustomIndexer implements FileChangeListener
 {
 
 	public static final String UNRESOLVED_TYPE = "!!UNRESOLVED!!";
 	private final static Pattern CLOSURECLASS = Pattern.compile(".*?\\$\\d+\\z");
 	static JOTLoggerLocation log = new JOTLoggerLocation(FanIndexer.class);
 	private final FanIndexerThread indexerThread;
 	public static volatile boolean shutdown = false;
 	//TODO: will that work or should they all be in the same fifo stack
 	Hashtable<String, Long> fanSrcToBeIndexed = new Hashtable<String, Long>();
 	Hashtable<String, Long> fanPodsToBeIndexed = new Hashtable<String, Long>();
 	Hashtable<String, Long> toBeDeleted = new Hashtable<String, Long>();
 	private FanJarsIndexer jarsIndexer;
 	private boolean alreadyWarned;
 
 	public FanIndexer()
 	{
 		super();
 		indexerThread = new FanIndexerThread();
 		indexerThread.start();
 		if (FanPlatform.getInstance(false).isConfigured())
 		{
 			indexAll(false);
 		}
 	}
 
 	synchronized void warnIfNecessary()
 	{
 		if (!alreadyWarned)
 		{
 			alreadyWarned = true;
 			NotifyDescriptor desc = new NotifyDescriptor.Message("Initial Fantom/Java API Indexing just started\nThis might take a while and use a lot of CPU (once).\nSome features such as completion will not be available until it's completed.", NotifyDescriptor.WARNING_MESSAGE);
 			DialogDisplayer.getDefault().notify(desc);
 		}
 	}
 
 	public void indexAll(boolean backgroundJava)
 	{
 		MainIndexer idx = new MainIndexer(backgroundJava);
 		idx.start();
 	}
 
 	public FanJarsIndexer getJarsIndexer()
 	{
 		return jarsIndexer;
 	}
 
 	@Override
 	protected void index(Iterable<? extends Indexable> iterable, Context context)
 	{
 		for (Indexable indexable : iterable)
 		{
 			requestIndexing(indexable.getURL().getPath());
 		}
 	}
 
 	public void requestDelete(String path)
 	{
 		toBeDeleted.put(path, new Date().getTime());
 	}
 
 	/**
 	 * all Fantom indexing should be requested through here (no direct call to index()
 	 * for threading safety
 	 * @param path
 	 */
 	public void requestIndexing(String path)
 	{
 
 		boolean isPod = path.toLowerCase().endsWith(".pod");
 
 		FileObject fo = FileUtil.toFileObject(new File(path));
 		if (fo.getNameExt().equalsIgnoreCase("sys.pod"))
 		{
 			warnIfNecessary();
 		}
 		if (fo.getNameExt().equalsIgnoreCase("sys.pod"))
 		{
 			warnIfNecessary();
 		}
 		if (isPod)
 		{
 			fanPodsToBeIndexed.put(path, new Date().getTime());
 		} else if (!FileUtil.isParentOf(FanPlatform.getInstance().getFanHome(), fo))
 		{
 			fanSrcToBeIndexed.put(path, new Date().getTime());
 		}
 	}
 
 	// TODO: It would be MUCH faster to just parse what we need (types/slots)
 	// Might need a separte ANTLR grammar though.
 	private void indexSrc(String path)
 	{
 		FanPlatform platform = FanPlatform.getInstance(false);
 		if (platform.isConfigured())
 		{
 			// Don't do Fantom distro sources since we have binaries (faster)
 			log.info(platform.getFanHome()+" VS "+path);
 			if (FileUtil.isParentOf(platform.getFanHome(), FileUtil.toFileObject(new File(path))))
 			{
 				log.info("Skipping: "+path);
 				return;
 			}
 		}else{log.info("Platform not ready to index: "+path);}
 
 		long then = new Date().getTime();
 		log.info("Indexing requested for: " + path);
 		// Get a snaphost of the source
 		File f = new File(path);
 
 		FileObject fo = FileUtil.toFileObject(f);
 		Source source = Source.create(fo);
 		Snapshot snapshot = source.createSnapshot();
 		// Parse the snaphot
 		NBFanParser parser = new NBFanParser();
 		try
 		{
 			parser.parse(snapshot);
 		} catch (Throwable e)
 		{
 			log.exception("Parsing failed for: " + path, e);
 			return;
 		}
 		Result result = parser.getResult();
 		long now = new Date().getTime();
 		log.debug("Indexing - parsing done in " + (now - then) + " ms for: " + path);
 		// Index the parsed doc
 		indexSrc(path, result);
 		now = new Date().getTime();
 		log.debug("Indexing completed in " + (now - then) + " ms for: " + path);
 	}
 
 	private void indexSrc(String path, Result parserResult)
 	{
 		log.debug("Indexing parsed result for : " + path);
 
 		FanParserTask fanResult = (FanParserTask) parserResult;
 		//indexSrcDoc(path, fanResult.getRootScope()); // FIXME
 	}
 
 	/**
 	 * Index the document in the DB, using the root scope.
 	 * @param doc
 	 * @param indexable
 	 * @param rootScope
 	 */
 	/*private void indexSrcDoc(String path, FanRootScope rootScope)
 	{
 		FanDocument doc = null;
 		try
 		{
 			if (rootScope != null)
 			{
 				// create / update the doument
 				doc = FanDocument.findOrCreateOne(null, path);
 				doc.setPath(path);
 				doc.setTstamp(0L);
 				doc.setIsSource(true);
 				doc.save();
 
 				// Update the  "using" / try to be smart as to not delete / recreate all everytime.
 				Vector<FanDocUsing> usings = FanDocUsing.findAllForDoc(null, doc.getId());
 				Vector<FanType> types = FanType.findAllForDoc(null, doc.getId());
 
 				Vector<String> addedUsings = new Vector();
 				for (FanResolvedType type : rootScope.getUsing().values())
 				{
 					String sig = type.getAsTypedType();
 					int foundIdx = -1;
 					for (int i = 0; i != usings.size(); i++)
 					{
 						FanDocUsing using = usings.get(i);
 						if (using.getType().equals(sig))
 						{
 							foundIdx = i;
 							break;
 						}
 					}
 					if (foundIdx != -1)
 					{
 						// already in there, leave it alone
 						usings.remove(foundIdx);
 					} else
 					{
 						// there can be duplicates because of the way rootscope usings works
 						if (!addedUsings.contains(sig))
 						{
 							addedUsings.add(sig);
 							// new one, creating it
 							FanDocUsing using = new FanDocUsing();
 							using.setDocumentId(doc.getId());
 							using.setType(sig);
 							using.save();
 						}
 					}
 				}
 
 				// types
 				for (FanAstScope child : rootScope.getChildren())
 				{
 					// should be but check anyway in case of future change
 					if (child instanceof FanTypeScope)
 					{
 						FanTypeScope typeScope = (FanTypeScope) child;
 						JOTSQLCondition cond = new JOTSQLCondition("qualifiedName", JOTSQLCondition.IS_EQUAL, typeScope.getQName());
 						FanType dbType = (FanType) JOTQueryBuilder.selectQuery(null, FanType.class).where(cond).findOrCreateOne();
 						if (!dbType.isNew())
 						{
 							for (int i = 0; i != types.size(); i++)
 							{
 								FanType t = types.get(i);
 								if (t.getId() == dbType.getId())
 								{
 									types.remove(i);
 									break;
 								}
 							}
 						}
 						dbType.setSrcDocId(doc.getId());
 						dbType.setKind(typeScope.getKind().value());
 						dbType.setIsAbstract(typeScope.hasModifier(FanAstScopeVarBase.ModifEnum.ABSTRACT));
 						dbType.setIsConst(typeScope.hasModifier(FanAstScopeVarBase.ModifEnum.CONST));
 						dbType.setIsFinal(typeScope.hasModifier(FanAstScopeVarBase.ModifEnum.FINAL));
 						dbType.setQualifiedName(typeScope.getQName());
 						dbType.setSimpleName(typeScope.getName());
 						dbType.setPod(typeScope.getPod());
 						dbType.setProtection(typeScope.getProtection());
 						dbType.setIsFromSource(true);
 
 						dbType.save();
 
 						// Try to reuse existing db entries.
 						Vector<FanTypeInheritance> currentInh = FanTypeInheritance.findAllForMainType(null, typeScope.getQName());
 						for (FanResolvedType item : typeScope.getInheritedItems())
 						{
 							String mainType = typeScope.getQName();
 							String inhType = item.isResolved() ? item.getDbType().getQualifiedName() : item.getAsTypedType();
 							FanTypeInheritance inh = null;
 							for (int i = 0; i != currentInh.size(); i++)
 							{
 								FanTypeInheritance cur = currentInh.get(i);
 								if (cur.getMainType().equals(mainType) && cur.getInheritedType().equals(inhType))
 								{
 									inh = cur;
 									currentInh.remove(i);
 									break;
 								}
 							}
 							if (inh == null)
 							{
 								// new one, creating it
 								inh = new FanTypeInheritance();
 							}
 							inh.setMainType(mainType);
 							inh.setInheritedType(inhType);
 							inh.save();
 						}
 						// old inherited types
 						for (FanTypeInheritance oldInh : currentInh)
 						{
 							//System.out.println("inh delete " + oldInh.getInheritedType() + "::" + oldInh.getMainType());
 							oldInh.delete();
 						}
 
 						// Slots
 						// Try to reuse existing db entries.
 						Vector<FanSlot> currentSlots = FanSlot.findAllForType(dbType.getId());
 						for (FanAstScopeVarBase slot : typeScope.getScopeVars())
 						{
 							// determine kind of slot
 							FanModelConstants.SlotKind kind = FanModelConstants.SlotKind.FIELD;
 							if (slot instanceof FanMethodScopeVar)
 							{
 								if (((FanMethodScopeVar) slot).isCtor())
 								{
 									kind = FanModelConstants.SlotKind.CTOR;
 								} else
 								{
 									kind = FanModelConstants.SlotKind.METHOD;
 								}
 							} else if (slot instanceof FanFieldScopeVar)
 							{
 								kind = FanModelConstants.SlotKind.FIELD;
 							} else
 							{
 								throw new RuntimeException("Unexpected Slot kind: " + slot.getClass().getName());
 							}
 
 							JOTSQLCondition cond2 = new JOTSQLCondition("typeId", JOTSQLCondition.IS_EQUAL, dbType.getId());
 							JOTSQLCondition cond3 = new JOTSQLCondition("name", JOTSQLCondition.IS_EQUAL, slot.getName());
 							FanSlot dbSlot = (FanSlot) JOTQueryBuilder.selectQuery(null, FanSlot.class).where(cond2).where(cond3).findOrCreateOne();
 							if (!dbSlot.isNew())
 							{
 								for (int i = 0; i != currentSlots.size(); i++)
 								{
 									FanSlot s = currentSlots.get(i);
 									if (s.getId() == dbSlot.getId())
 									{
 										currentSlots.remove(i);
 										break;
 									}
 								}
 							}
 							dbSlot.setTypeId(dbType.getId());
 							dbSlot.setSlotKind(kind.value());
 							FanResolvedType slotType = slot.getType();
 							dbSlot.setReturnedType(slotType.toTypeSig(true));
 							dbSlot.setName(slot.getName());
 							dbSlot.setIsAbstract(slot.hasModifier(ModifEnum.ABSTRACT));
 							dbSlot.setIsNative(slot.hasModifier(ModifEnum.NATIVE));
 							dbSlot.setIsOverride(slot.hasModifier(ModifEnum.OVERRIDE));
 							dbSlot.setIsStatic(slot.hasModifier(ModifEnum.STATIC));
 							dbSlot.setIsVirtual(slot.hasModifier(ModifEnum.VIRTUAL));
 							dbSlot.setIsConst(slot.hasModifier(ModifEnum.CONST));
 							dbSlot.setProtection(slot.getProtection());
 							dbSlot.setIsNullable(slotType.isNullable());
 
 							dbSlot.save();
 
 							// deal with parameters of method/ctor
 							if (slot instanceof FanMethodScopeVar)
 							{
 								FanMethodScopeVar method = (FanMethodScopeVar) slot;
 								Hashtable<String, FanResolvedType> parameters = method.getParameters();
 
 								// Try to reuse existing db entries.
 								Vector<FanMethodParam> currentParams = FanMethodParam.findAllForSlot(dbSlot.getId());
 								for (String paramName : parameters.keySet())
 								{
 									FanResolvedType paramResult = parameters.get(paramName);
 									JOTSQLCondition cond4 = new JOTSQLCondition("slotId", JOTSQLCondition.IS_EQUAL, dbSlot.getId());
 									FanMethodParam dbParam = (FanMethodParam) JOTQueryBuilder.selectQuery(null, FanMethodParam.class).where(cond4).findOrCreateOne();
 									if (!dbParam.isNew())
 									{
 										for (int i = 0; i != currentParams.size(); i++)
 										{
 											FanMethodParam p = currentParams.get(i);
 											if (p.getId() == dbParam.getId())
 											{
 												currentParams.remove(i);
 												break;
 											}
 										}
 									}
 									dbParam.setSlotId(dbSlot.getId());
 									dbParam.setName(paramName);
 									String pType = UNRESOLVED_TYPE; // can that happen ?
 									if (paramResult.isResolved())
 									{
 										pType = paramResult.getDbType().getQualifiedName();
 									}
 									dbParam.setQualifiedType(pType);
 									dbParam.setIsNullable(paramResult.isNullable());
 									//dbParam.setHasDefault();
 									dbParam.save();
 
 								} // end param loop
 								// Whatever param wasn't removed from the vector is not needed anymore.
 								for (FanMethodParam param : currentParams)
 								{
 									param.delete();
 								}
 							}
 						} // end slot loop
 						// Whatever slot wasn't removed from the vector is not needed anymore.
 						for (FanSlot s : currentSlots)
 						{
 							s.delete();
 						}
 
 					}
 				} // end type loop
 
 				// all went well, set the tstamp - mark as good
 				doc.setTstamp(new Date().getTime());
 				doc.save();
 
 				// remove old usings
 				for (FanDocUsing using : usings)
 				{
 					using.delete();
 				}
 				// remove old types
 				for (FanType t : types)
 				{
 					//System.out.println("type delete " + t.getQualifiedName());
 					t.delete();
 				}
 			}
 
 		} catch (Exception e)
 		{
 			log.exception("Indexing Failed for: " + path, e);
 			try
 			{
 				// remove the incomplete doc ... wil try again next time.
 				if (doc != null)
 				{
 					doc.delete();
 				}
 			} catch (Exception e2)
 			{
 				log.exception("Indexing 'rollback' failed for: " + path, e);
 			}
 		}
 	}*/
 
 	public static boolean checkIfNeedsReindexing(String path, long tstamp)
 	{
 		JOTSQLCondition cond = new JOTSQLCondition("path", JOTSQLCondition.IS_EQUAL, path);
 		try
 		{
 			FanDocument doc = (FanDocument) JOTQueryBuilder.selectQuery(null, FanDocument.class).where(cond).findOne();
 			if (doc != null)
 			{
 				long indexedTime = doc.getTstamp();
 				if (indexedTime >= tstamp)
 				{
 					return false;
 				}
 			}
 		} catch (Exception e)
 		{
 			log.exception("FanDocument search exception", e);
 		}
 		return true;
 	}
 
 	public void indexFantomPods(boolean runInBackground)
 	{
 		FanPlatform platform = FanPlatform.getInstance(false);
 		if (platform.isConfigured())
 		{
 			try
 			{
 				String podsDir = platform.getPodsDir();
 				File f = new File(podsDir);
 				// listen to changes in pod folder
 				FileUtil.addRecursiveListener(this, f);
 				// index the pods if not up to date
 				File[] pods = f.listFiles();
 
 				for (File pod : pods)
 				{
 					String path = pod.getAbsolutePath();
 					if (checkIfNeedsReindexing(path, pod.lastModified()))
 					{
 						requestIndexing(path);
 					}
 				}
 			} catch (Throwable t)
 			{
 				log.exception("Pod indexing thread error", t);
 
 			}
 		}
 		if (!runInBackground)
 		{
 			while (!(shutdown || fanPodsToBeIndexed.isEmpty()))
 			{
 				try
 				{
 					Thread.sleep(100);
 					Thread.yield();
 				} catch (Exception e)
 				{
 					log.exception("waiting for pods indexign to complete", e);
 				}
 			}
 		}
 	}
 
 	private void indexPod(String pod)
 	{
 		if (pod.toLowerCase().endsWith(".pod"))
 		{
 			FanDocument doc = null;
 			try
 			{
 
 				//ZipFile zpod = new ZipFile(pod);
 				FPod fpod = new FPod(null, FStore.makeZip(new File(pod)));
 				fpod.readFully();
 				log.info("Indexing pod: " + pod);
 				// Create the document
 				doc = FanDocument.findOrCreateOne(null, pod);
 
 				doc.setPath(pod);
 				doc.setTstamp(0L);
 				doc.setIsSource(false);
 				doc.save();
 				
 				Vector<FanType> types = FanType.findAllForDoc(null, doc.getId());
 				for (FType type : fpod.types)
 				{
 					FTypeRef typeRef = type.pod.typeRef(type.self);
 					String sig = typeRef.signature;
 
 					int flags = type.flags;
 					// Skipping "internal" classes - closures and the likes
 					// synthetic means generated by compiler
 
 					if (hasFlag(flags, FConst.Synthetic) || CLOSURECLASS.matcher(typeRef.typeName).matches())
 					{
 						continue;
 					}
 					log.debug("Indexing Pod Type: " + sig);
 
 					JOTSQLCondition cond = new JOTSQLCondition("qualifiedName", JOTSQLCondition.IS_EQUAL, sig);
 					FanType dbType = (FanType) JOTQueryBuilder.selectQuery(null, FanType.class).where(cond).findOrCreateOne();
 
 					if (!dbType.isNew())
 					{
 						for (int i = 0; i != types.size(); i++)
 						{
 							FanType t = types.get(i);
 							if (t.getId() == dbType.getId())
 							{
 								types.remove(i);
 								break;
 							}
 						}
 					}
 
 					dbType.setBinDocId(doc.getId());
 					dbType.setKind(getKind(type));
 
 					dbType.setIsAbstract(hasFlag(flags,
 						FConst.Abstract));
 					dbType.setIsConst(hasFlag(flags, FConst.Const));
 					dbType.setIsFinal(hasFlag(flags, FConst.Final));
 					dbType.setQualifiedName(sig);
 					dbType.setSimpleName(typeRef.typeName);
 					dbType.setPod(typeRef.podName);
 					dbType.setProtection(getProtection(type.flags));
 					dbType.setIsFromSource(
 						false);
 
 					dbType.save();
 					// Slots
 					// Try to reuse existing db entries.
 					Vector<FanSlot> currentSlots = FanSlot.findAllForType(dbType.getId());
 					Vector<FSlot> slots = new Vector();
 					slots.addAll(Arrays.asList(type.fields));
 					// It's a bit odd but type.methods has the fields in as well
 					// I guess because Fan creates "internal" field getter/setters ?
 					for (FMethod m : type.methods)
 					{
 						boolean isField = false;
 						for (FSlot s : slots)
 						{
 							if (s.name.equals(m.name))
 							{
 								isField = true;
 							}
 						}
 						if (!isField)
 						{
 							slots.add(m);
 						}
 					}
 					for (FSlot slot : slots)
 					{
 						// determine kind of slot
 						FanModelConstants.SlotKind kind = FanModelConstants.SlotKind.FIELD;
 						FTypeRef retType = null;
 						if (slot instanceof FField)
 						{
 							kind = FanModelConstants.SlotKind.FIELD;
 							retType = type.pod.typeRef(((FField) slot).type);
 						} else if (slot instanceof FMethod)
 						{
 							retType = type.pod.typeRef(((FMethod) slot).ret);
 							if (hasFlag(slot.flags, FConst.Ctor))
 							{
 								kind = FanModelConstants.SlotKind.CTOR;
 							} else
 							{
 								kind = FanModelConstants.SlotKind.METHOD;
 							}
 						} else
 						{
 							throw new RuntimeException("Unexpected Slot kind: " + slot.getClass().getName());
 						}
 
 						JOTSQLCondition cond2 = new JOTSQLCondition("typeId", JOTSQLCondition.IS_EQUAL, dbType.getId());
 						JOTSQLCondition cond3 = new JOTSQLCondition("name", JOTSQLCondition.IS_EQUAL, slot.name);
 						FanSlot dbSlot = (FanSlot) JOTQueryBuilder.selectQuery(null, FanSlot.class).where(cond2).where(cond3).findOrCreateOne();
 						if (!dbSlot.isNew())
 						{
 							for (int i = 0; i != currentSlots.size(); i++)
 							{
 								FanSlot s = currentSlots.get(i);
 								if (s.getId() == dbSlot.getId())
 								{
 									currentSlots.remove(i);
 									break;
 								}
 							}
 						}
 						dbSlot.setTypeId(dbType.getId());
 						dbSlot.setSlotKind(kind.value());
 						dbSlot.setReturnedType(retType.signature);
 						dbSlot.setName(slot.name);
 						dbSlot.setIsAbstract(hasFlag(slot.flags, FConst.Abstract));
 						dbSlot.setIsNative(hasFlag(slot.flags, FConst.Native));
 						dbSlot.setIsOverride(hasFlag(slot.flags, FConst.Override));
 						dbSlot.setIsStatic(hasFlag(slot.flags, FConst.Static));
 						dbSlot.setIsVirtual(hasFlag(slot.flags, FConst.Virtual));
 						dbSlot.setIsConst(hasFlag(slot.flags, FConst.Const));
 						dbSlot.setProtection(getProtection(slot.flags));
 						dbSlot.setIsNullable(retType.isNullable());
 
 						dbSlot.save();
 
 						// deal with parameters of method/ctor
 						if (slot instanceof FMethod)
 						{
 
 							FMethod method = (FMethod) slot;
 							FMethodVar[] parameters = method.params();
 							// Try to reuse existing db entries.
 							Vector<FanMethodParam> currentParams = FanMethodParam.findAllForSlot(dbSlot.getId());
 							for (FMethodVar param : parameters)
 							{
 								JOTSQLCondition cond4 = new JOTSQLCondition("slotId", JOTSQLCondition.IS_EQUAL, dbSlot.getId());
 								FanMethodParam dbParam = (FanMethodParam) JOTQueryBuilder.selectQuery(null, FanMethodParam.class).where(cond4).findOrCreateOne();
 								if (!dbParam.isNew())
 								{
 									for (int i = 0; i != currentParams.size(); i++)
 									{
 										FanMethodParam p = currentParams.get(i);
 										if (p.getId() == dbParam.getId())
 										{
 											currentParams.remove(i);
 											break;
 										}
 									}
 								}
 								FTypeRef tRef = type.pod.typeRef(param.type);
 								dbParam.setSlotId(dbSlot.getId());
 								dbParam.setName(param.name);
 								dbParam.setQualifiedType(tRef.signature);
 								dbParam.setIsNullable(tRef.isNullable());
 								dbParam.setHasDefault(param.def != null);
 
 								dbParam.save();
 
 							} // end param loop
 							// Whatever param wasn't removed from the vector is not needed anymore.
 							for (FanMethodParam param : currentParams)
 							{
 								param.delete();
 							}
 						}
 					} // end slot loop
 					// Whatever slot wasn't removed from the vector is not needed anymore.
 					for (FanSlot s : currentSlots)
 					{
 						s.delete();
 					}
 
 					// Deal with Inheritance
 					Vector<FTypeRef> inhTypes = new Vector<FTypeRef>();
 					if (type.base >= 0 && type.base != 65535) // 65535 seem to eb value for a type with no base (Obj?)
 					{
 						inhTypes.add(type.pod.typeRef(type.base));
 					}
 
 					for (int t : type.mixins)
 					{
 						inhTypes.add(type.pod.typeRef(t));
 					}
 					// Try to reuse existing db entries.
 					Vector<FanTypeInheritance> currentInh = FanTypeInheritance.findAllForMainType(null, typeRef.signature);
 					for (FTypeRef item : inhTypes)
 					{
 						String mainType = typeRef.signature;
 						String inhType = item.signature;
 						if (inhType.equals("sys::Obj")
 							|| inhType.equals("sys::Enum"))
 						{
 							// Those types are implied, no need to pollute the DB
 							continue;
 						}
 						int foundIdx = -1;
 						for (int i = 0; i != currentInh.size(); i++)
 						{
 							FanTypeInheritance cur = currentInh.get(i);
 							if (cur.getMainType().equals(mainType) && cur.getInheritedType().equals(inhType))
 							{
 								foundIdx = i;
 								break;
 							}
 						}
 						if (foundIdx != -1)
 						{
 							// already in there, leave it alone
 							currentInh.remove(foundIdx);
 						} else
 						{
 							// new one, creating it
 							FanTypeInheritance inh = new FanTypeInheritance();
 							inh.setMainType(mainType);
 							inh.setInheritedType(inhType);
 							inh.save();
 							//System.out.println("saving inh: " + inh.getMainType() + " " + inh.getInheritedType() + " " + inh.getId());
 						}
 					} // end inh
 					// Whatever wasn't removed from the vector is not needed anymore.
 					for (FanTypeInheritance inh : currentInh)
 					{
 						inh.delete();
 					}
 
 					// allow for quicker exit on shutdown
 					if (shutdown)
 					{
 						break;
 					}
 				} // end type
 
 				// all went well, set the tstamp - mark as good
 				doc.setTstamp(new Date().getTime());
 				doc.save();
 
 				for (FanType t : types)
 				{
 					t.delete();
 				}
 			} catch (Exception e)
 			{
 				log.exception("Indexing failed for: " + pod, e);
 				try
 				{
 					// remove broken entry, will try again next time
 					if (doc != null)
 					{
 						doc.delete();
 
 
 					}
 				} catch (Exception e2)
 				{
 					log.exception("Indexing 'rollback' failed for: " + pod, e);
 				}
 			}
 		}
 	}
 
 	private int getKind(FType type)
 	{
 		if (hasFlag(type.flags, FConst.Mixin))
 		{
 			return FanAstScopeVarBase.VarKind.TYPE_MIXIN.value();
 		}
 		if (hasFlag(type.flags, FConst.Enum))
 		{
 			return FanAstScopeVarBase.VarKind.TYPE_ENUM.value();
 		} // class is default
 		return FanAstScopeVarBase.VarKind.TYPE_CLASS.value();
 	}
 
 	private boolean hasFlag(int flags, int flag)
 	{
 		return (flags & flag) != 0;
 	}
 
 	public static void shutdown()
 	{
 		FanJarsIndexer.shutdown();
 		shutdown = true;
 	}
 
 	private int getProtection(int flags)
 	{
 		if (hasFlag(flags, FConst.Private))
 		{
 			return ModifEnum.PRIVATE.value();
 		}
 		if (hasFlag(flags, FConst.Protected))
 		{
 			return ModifEnum.PROTECTED.value();
 		}
 		if (hasFlag(flags, FConst.Internal))
 		{
 			return ModifEnum.INTERNAL.value();
 		} // default is public
 		return ModifEnum.PUBLIC.value();
 	}
 
 	public static String getPodDoc(String podName)
 	{
 		if (FanPlatform.getInstance(false).isConfigured())
 		{
 			Pod pod = null;
 			try
 			{
 				pod = Pod.find(podName);
 			} catch (RuntimeException e)
 			{
 				log.debug("Pod doc not found for " + podName);
 			}
 			if (pod != null)
 			{
				return fanDocToHtml(pod.doc());
 			}
 		}
 		return null;
 	}
 
 	public static String getSlotDoc(FanSlot slot)
 	{
 		FanType type = FanType.findByID(slot.getTypeId());
 		if (type != null)
 		{
 			String sig = type.getQualifiedName() + "." + slot.getName();
 			try
 			{
 				Slot fslot = Slot.find(sig);
 				if (fslot != null)
 				{
 					return fanDocToHtml(fslot.doc());
 				}
 			} catch (Throwable t)
 			{
 				// Fantom runtime exception if type ! found
 				log.debug(t.toString());
 			}
 		}
 		return null;
 	}
 
 	public static String getDoc(FanType type)
 	{
 		if (FanPlatform.getInstance(false).isConfigured())
 		{
 			try
 			{
 				Pod pod = Pod.find(type.getPod());
 				Type t = pod.type(type.getSimpleName());
 				if (t != null)
 				{
 					return fanDocToHtml(t.doc());
 				}
 			} catch (RuntimeException e)
 			{
 				log.debug("Type doc not found for " + type);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Parse Fandoc text into HTML using fan's builtin parser.
 	 * @param fandoc
 	 * @return
 	 */
 	public static String fanDocToHtml(String fandoc)
 	{
 		if (fandoc == null)
 		{
 			return null;
 		}
 		FanPlatform platform = FanPlatform.getInstance(false);
 		if (!platform.isConfigured())
 		{
 			return fandoc;
 		}
 		String html = fandoc;
 		try
 		{
 			FanObj parser = (FanObj) Type.find("fandoc::FandocParser").make();
 			FanObj doc = (FanObj) parser.typeof().method("parseStr").call(parser, fandoc);
 			Buf buf = Buf.make();
 			FanObj writer = (FanObj) Type.find("fandoc::HtmlDocWriter").method("make").call(buf.out());
 			doc.typeof().method("write").call(doc, writer);
 			html = buf.flip().readAllStr();
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		} //System.out.println("Html doc: "+html);
 		return html;
 	}
 
 	private void cleanupOldDocs()
 	{
 		try
 		{
 			Vector<FanDocument> docs = JOTQueryBuilder.selectQuery(null, FanDocument.class).find().getAllResults();
 			for (FanDocument doc : docs)
 			{
 				String path = doc.getPath();
 				if (!new File(path).exists())
 				{
 					log.info("Removing entries for removed document: " + path);
 					doc.delete();
 				}
 			}
 		} catch (Exception e)
 		{
 			log.exception("Error deleting outdated docs", e);
 		}
 	}
 
 	//*********** File listeners ****************************
 	public void fileFolderCreated(FileEvent fe)
 	{
 		// Listen for changes
 		String path = fe.getFile().getPath();
 		log.debug("Folder created: " + path);
 		FileUtil.addFileChangeListener(this, FileUtil.toFile(fe.getFile()));
 	}
 
 	public void fileDataCreated(FileEvent fe)
 	{
 		String path = fe.getFile().getPath();
 		log.debug("File created: " + path);
 		requestIndexing(
 			path);
 	}
 
 	public void fileChanged(FileEvent fe)
 	{
 		String path = fe.getFile().getPath();
 		log.debug("File changed: " + path);
 		requestIndexing(
 			path);
 	}
 
 	public void fileDeleted(FileEvent fe)
 	{
 		// synced because we don't want to do it at the same time as the thread
 		String path = fe.getFile().getPath();
 		log.debug("File deleted: " + path);
 		toBeDeleted.put(path, new Date().getTime());
 	}
 
 	public void fileRenamed(FileRenameEvent fre)
 	{
 		// synced because we don't want to do it at the same time as the thread
 		FileObject src = (FileObject) fre.getSource();
 		log.debug("File renamed: " + src.getPath() + " -> " + fre.getFile().getPath());
 		//TODO add this to a hashtable and do it in the thread
 		FanDocument.renameDoc(src.getPath(), fre.getFile().getPath());
 	}
 
 	public void fileAttributeChanged(FileAttributeEvent fae)
 	{
 		int bkpt=0;
 		// don't care
 	}
 
 	/*********************************************************************
 	 *  Indexer Thread class
 	 * All indexing request should go through here to avoid issues.
 	 */
 	class FanIndexerThread extends Thread implements Runnable
 	{
 
 		@Override
 		public void run()
 		{
 			while (!shutdown)
 			{
 				try
 				{
 					Thread.yield();
 					sleep(100);
 				} catch (Exception e)
 				{
 				}
 				// to be deleted
 				Enumeration<String> tbd = toBeDeleted.keys();
 				{
 					while (tbd.hasMoreElements())
 					{
 						if (shutdown)
 						{
 							return;
 						}
 						String path = tbd.nextElement();
 
 						FanDocument doc = FanDocument.findByPath(path);
 						try
 						{
 							if (doc != null)
 							{
 								doc.delete();
 							}
 						} catch (Exception e)
 						{
 							log.exception("Error deleting doc", e);
 						}
 					}
 				}
 				// always do binaries first
 				do
 				{
 					// Usig keys() since it uses a "snapshot"
 					// no concurrentmodif error
 					// also nextElement() should be safe since we only remove elements from within here
 					// elems can be added outside ... but that should be fine.
 					Enumeration<String> it = fanPodsToBeIndexed.keys();
 					while (it.hasMoreElements())
 					{
 						if (shutdown)
 						{
 							return;
 						}
 						String path = it.nextElement();
 						Long l = fanPodsToBeIndexed.get(path);
 						long now = new Date().getTime();
 						if (l.longValue() < now - 1000)
 						{
 							fanPodsToBeIndexed.remove(path);
 							indexPod(path);
 						}
 					}
 				} while (!fanPodsToBeIndexed.isEmpty());
 				// then do the sources
 				Enumeration<String> it = fanSrcToBeIndexed.keys();
 				while (it.hasMoreElements())
 				{
 					if (shutdown)
 					{
 						return;
 					}
 					String path = it.nextElement();
 					Long l = fanSrcToBeIndexed.get(path);
 					// Hasn't changed in a couple seconds
 					long now = new Date().getTime();
 					if (l.longValue() < now - 2000)
 					{
 						fanSrcToBeIndexed.remove(path);
 						indexSrc(path);
 					}
 				}
 			}
 		}
 	}
 
 	class MainIndexer extends Thread implements Runnable
 	{
 
 		volatile boolean done = false;
 		private final boolean bg;
 
 		public MainIndexer(boolean backgroundJava)
 		{
 			super();
 			this.bg = backgroundJava;
 		}
 
 		@Override
 		public void run()
 		{
 			done = false;
 			alreadyWarned = false;
 			// cleanup old docs
 			cleanupOldDocs();
 			// start the indexing thread
 			// index Fantom libs right aways
 			long then = new Date().getTime();
 			// we want to wait for pods, since we want them done first
 			indexFantomPods(false);
 			long now = new Date().getTime();
 			log.info("Fantom Pod Parsing completed in " + (now - then) + " ms.");
 			// sources indexes will be called  through scanStarted()
 			jarsIndexer = new FanJarsIndexer();
 			// Do this one in the background (might take a while and not needed for everyone)
 			jarsIndexer.indexJars(bg);
 			done = true;
 		}
 
 	}
 }
