 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package biz.bidi.archivee.components.compressor;
 
 import java.util.ArrayList;
 import java.util.HashMap;
import java.util.List;
 
 import org.bson.types.ObjectId;
 
 import biz.bidi.archivee.commons.ArchiveeConstants;
 import biz.bidi.archivee.commons.components.ArchiveeManagedComponent;
 import biz.bidi.archivee.commons.components.Component;
 import biz.bidi.archivee.commons.exceptions.ArchiveeException;
 import biz.bidi.archivee.commons.interfaces.ICompressorSender;
 import biz.bidi.archivee.commons.model.huffman.HuffmanObjectIdNode;
 import biz.bidi.archivee.commons.model.huffman.HuffmanObjectIdTree;
 import biz.bidi.archivee.commons.model.huffman.HuffmanWordNode;
 import biz.bidi.archivee.commons.model.huffman.HuffmanWordTree;
 import biz.bidi.archivee.commons.model.mongodb.Context;
 import biz.bidi.archivee.commons.model.mongodb.ContextIndex;
 import biz.bidi.archivee.commons.model.mongodb.ContextQueue;
 import biz.bidi.archivee.commons.model.mongodb.Dictionary;
 import biz.bidi.archivee.commons.model.mongodb.DictionaryEntry;
 import biz.bidi.archivee.commons.model.mongodb.DictionaryQueue;
 import biz.bidi.archivee.commons.model.mongodb.Pattern;
 import biz.bidi.archivee.commons.model.mongodb.PatternPath;
 import biz.bidi.archivee.commons.model.mongodb.Template;
 import biz.bidi.archivee.commons.model.mongodb.TemplateDictionary;
 import biz.bidi.archivee.commons.model.xml.CompressorMessage;
 import biz.bidi.archivee.commons.model.xml.PatternMessage;
 import biz.bidi.archivee.commons.model.xml.enums.CompressorMessageType;
 import biz.bidi.archivee.commons.utils.ArchiveeByteUtils;
 import biz.bidi.archivee.commons.utils.ArchiveeDateUtils;
 import biz.bidi.archivee.commons.utils.ArchiveePatternUtils;
 import biz.bidi.archivee.components.compressor.commons.CompressorManager;
 import biz.bidi.archivee.components.listeners.parser.DateLevelLogParser;
 
 /**
  * @author Andrey Bidinotto
  * @email andreymoser@bidi.biz
  * @since Oct 2, 2012
  */
 public class Compressor extends ArchiveeManagedComponent implements ICompressor {
 
 	/**
 	 * The compressor sender
 	 */
 	protected ICompressorSender compressorSender;
 	/**
 	 * The level values
 	 */
 	protected ArrayList<String> levels; 
 	
 	public Compressor() {
 		try {
 			lockerDAO = CompressorManager.getInstance().getLockerDAO();
 			templateDAO = CompressorManager.getInstance().getTemplateDAO();
 			patternDAO = CompressorManager.getInstance().getPatternDAO();
 			
 			dictionaryQueueDAO = CompressorManager.getInstance().getDictionaryQueueDAO();
 			dictionaryDAO = CompressorManager.getInstance().getDictionaryDAO();
 			
 			contextQueueDAO = CompressorManager.getInstance().getContextQueueDAO();
 			contextIndexDAO = CompressorManager.getInstance().getContextIndexDAO();
 			contextDAO = CompressorManager.getInstance().getContextDAO();
 
 			templateDictionaryDAO = CompressorManager.getInstance().getTemplateDictionaryDAO();
 			
 			compressorSender = CompressorManager.getInstance().getCompressorSender();
 		} catch (ArchiveeException e) {
 			ArchiveeException.error(e, "Error in init Compressor component.", this);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see biz.bidi.archivee.components.compressor.ICompressor#compressData(biz.bidi.archivee.commons.model.xml.CompressorMessage)
 	 */
 	@Override
 	public void compressData(CompressorMessage message) throws ArchiveeException {
 		if(message.getContextQueueId() == null) {
 			throw new ArchiveeException("Invalid compressor message: context queue is null!",this,message);
 		}
 
 		if(!acquireLock(Component.COMPRESSOR.getValue(), message.getThreadId()))
 		{
 			compressorSender.sendCompressorMessage(message);
 			return;
 		}
 
 		try {
 			ContextQueue contextQueue = new ContextQueue();
 			contextQueue.setId(message.getContextQueueId());
 			for(ContextQueue cq : contextQueueDAO.find(contextQueue)) {
 				contextQueue = cq;
 				break;
 			}
 			if(contextQueue.getKey().getPatternId() == null) { //if not found
 				throw new ArchiveeException("Invalid compressor message: context queue doesn't exists!",message);
 			}
 			
 			if(message.getMessageType().equals(CompressorMessageType.CREATE_DICTIONARY)) {
 				ArrayList<Template> templates = findTemplates(contextQueue);
 				
 				for(Template template : templates) {
 					DictionaryQueue dictionaryQueue = new DictionaryQueue();
 					dictionaryQueue.getKey().setTemplateId(template.getId());
 					dictionaryQueue.getKey().setSequence(contextQueue.getKey().getSequence());
 					
 					for(DictionaryQueue dq : dictionaryQueueDAO.find(dictionaryQueue, ArchiveeConstants.DICTIONARY_QUEUE_TEMPLATE_QUERY)) {
 						Dictionary dictionary = new Dictionary();
 						dictionary.setKey(dq.getKey());
 
 						buildDictionary(dictionary, dq);
 						
 						dictionaryDAO.save(dictionary);
 						dictionaryQueueDAO.delete(dq,null);
 					}
 				}
 				compressorSender = CompressorManager.getInstance().getCompressorSender();
 				
 				message.setMessageType(CompressorMessageType.COMPRESS_DATA);
 				compressorSender.sendCompressorMessage(message);
 				return;
 			}
 			
 			if(message.getMessageType().equals(CompressorMessageType.COMPRESS_DATA)) {
 				compressContextQueueData(message, contextQueue);
 				contextQueueDAO.delete(contextQueue, null);
 				return;
 			}
 		} catch (ArchiveeException e) {
 			release(Component.COMPRESSOR.getValue(), message.getThreadId());
 			throw e;
 		} catch (Exception e) {
 			release(Component.COMPRESSOR.getValue(), message.getThreadId());
 			throw new ArchiveeException(e,this);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void compressContextQueueData(CompressorMessage message, ContextQueue contextQueue) throws ArchiveeException {
 		Pattern pattern = new Pattern();
 		pattern.setId(contextQueue.getKey().getPatternId());
 		for(Pattern p : patternDAO.find(pattern)) {
 			pattern = p;
 			break;
 		}
 		if(pattern.getId() == null) {
 			throw new ArchiveeException("Pattern not found on init for compressing bits",this,pattern,contextQueue,message);
 		}
 
 		Context context = new Context();
 		context.getKey().setAppId(pattern.getAppId());
 		context.getKey().setPatternId(contextQueue.getKey().getPatternId());
 		context.getKey().setSequence(contextQueue.getKey().getSequence());
 		context.setStartDate(contextQueue.getStartDate());
 		
 		//Template Dictionary creation
 		TemplateDictionary templateDictionary = new TemplateDictionary();
 		templateDictionary.setContextId(contextQueue.getId());
 		createTemplateDictionary(templateDictionary, contextQueue);
 
 		//Date Dictionary creation
 		long timeRange = contextQueue.getEndDate().getTime() - contextQueue.getStartDate().getTime();
 		context.setDateBitsLenght(ArchiveeByteUtils.getMaxBitsLength(timeRange));
 
 		ArrayList<Byte> data = new ArrayList<Byte>();
 		
 		levels = DateLevelLogParser.getLevelsList();
 		
 		if(levels == null || levels.size() == 0) {
 			throw new ArchiveeException("Levels not defined on properties file for compressing bits.",this,contextQueue);
 		}
 		
 		int levelBitsLength = ArchiveeByteUtils.getMaxBitsLength(levels.size());
 		
 		int bitOffset = 0;
 		
 		for(PatternMessage patternMessage : contextQueue.getMessages()) {
 			PatternPath patternPath = ArchiveePatternUtils.findPatternPath(patternMessage.getMessage(), pattern);
 			
 			if(patternPath == null || patternPath.getValues().size() == 0) {
 				throw new ArchiveeException("Pattern path not found while compressing bits",this,patternPath,contextQueue,patternMessage);
 			}
 			
 			Template template = findTemplate(patternPath, pattern);
 			
 			bitOffset = ArchiveeByteUtils.append(template.getId(), data, bitOffset, (HashMap) templateDictionary.getTemplateEntries());
 			
 			Long date = ArchiveeDateUtils.convertToDate(patternMessage.getDate()).getTime() - contextQueue.getStartDate().getTime();
 			bitOffset = ArchiveeByteUtils.append(data, bitOffset, date, context.getDateBitsLenght());
 			
 			int levelIndex = levels.indexOf(patternMessage.getLevel());
 			if(levelIndex < 0) {
 				throw new ArchiveeException("Levels not defined on properties file for compressing bits.",this,contextQueue);
 			}
 
 			bitOffset = ArchiveeByteUtils.append(data, bitOffset, levelIndex, levelBitsLength);
 			
 			ArrayList<String> words = ArchiveePatternUtils.getPatternValues(patternMessage.getMessage());		
 			
 			String word = "";
 			int index = 0;
 			int i = 0;
 			while(i < words.size() && index < patternPath.getValues().size()) {
 				
 				int j=0;
 				for(j=0; j < patternPath.getValues().get(index).getWords(); j++) {
 					word = words.get(i);
 					i++;
 					
 					Dictionary dictionary = new Dictionary();
 					dictionary.getKey().setElementIndex(index);
 					dictionary.getKey().setSubElementIndex(j);
 					dictionary.getKey().setTemplateId(template.getId());
 					dictionary.getKey().setSequence(contextQueue.getKey().getSequence());
 					
 					for(Dictionary dq : dictionaryDAO.find(dictionary, ArchiveeConstants.DICTIONARY_KEY_QUERY)) {
 						dictionary = dq;
 						break;
 					}
 					if(dictionary.getId() == null) {
 						throw new ArchiveeException("Dictionary not found for pattern path while compressing bits",this,word,dictionary,template,contextQueue,patternMessage);
 					}
 					
 					bitOffset = ArchiveeByteUtils.append(word, data, bitOffset, (HashMap) dictionary.getEntries());
 					
 					//Saves context index data
 					ContextIndex contextIndex = new ContextIndex();
 					contextIndex.getKey().setAppId(pattern.getAppId());
 					contextIndex.getKey().setWord(word);
 					for(ContextIndex ci : contextIndexDAO.find(contextIndex, ArchiveeConstants.CONTEXT_INDEX_KEY_QUERY)) {
 						contextIndex = ci;
 						break;
 					}
 					//TODO validate context index
 					if(contextIndex != null && contextIndex.getId() != null) {
 						contextIndexDAO.save(contextIndex);
 					}
 				}
 				index++;
 			}
 			
 		}
 		
 		context.setData(data.toArray(new Byte[data.size()]));
 		contextDAO.save(context);
 		
 	}
 
 	/**
 	 * @param dictionary
 	 * @param dictQueue
 	 * @throws ArchiveeException 
 	 */
 	private void buildDictionary(Dictionary dictionary,DictionaryQueue dictQueue) throws ArchiveeException {
 		HuffmanWordTree huffmanWordTree = new HuffmanWordTree();
 		huffmanWordTree.buildTree(dictQueue.getCounts());
 		
 		for(String word : dictQueue.getCounts().keySet()) {
 			HuffmanWordNode node = huffmanWordTree.getNode(word);
 			
 			if(node == null) {
 				throw new ArchiveeException("Not found huffman node for dictionary creation: " + word,this,huffmanWordTree, dictQueue, dictionary);
 			}
 				
 			DictionaryEntry dictionaryEntry = new DictionaryEntry();
 			dictionaryEntry.setBitsLength(node.getLevel());
 			dictionaryEntry.setBytes(node.getCode());
 			dictionaryEntry.setCount(dictQueue.getCounts().get(word));
 			
 			dictionary.getEntries().put(word, dictionaryEntry);
 			dictionary.getEntriesIndex().put(node.getCode(), word);
 		}
 		
 	}
 	
 	/**
 	 * @param message
 	 * @param contextQueue
 	 * @return
 	 * @throws ArchiveeException 
 	 */
 	private void createTemplateDictionary(TemplateDictionary templateDictionary, ContextQueue contextQueue) throws ArchiveeException {
 		HuffmanObjectIdTree huffmanObjectIdTree = new HuffmanObjectIdTree();
 		huffmanObjectIdTree.buildTree(contextQueue.getTemplateCounts());
 		
 		for(ObjectId templateId : contextQueue.getTemplateCounts().keySet()) {
 			HuffmanObjectIdNode node = huffmanObjectIdTree.getNode(templateId);
 			
 			if(node == null) {
 				throw new ArchiveeException("Not found huffman node for template dictionary creation: " + templateId,this, huffmanObjectIdTree, contextQueue, templateDictionary);
 			}
 				
 			DictionaryEntry dictionaryEntry = new DictionaryEntry();
 			dictionaryEntry.setBitsLength(node.getLevel());
 			dictionaryEntry.setBytes(node.getCode());
 			dictionaryEntry.setCount(contextQueue.getTemplateCounts().get(templateId));
 			
 			templateDictionary.getTemplateEntries().put(templateId, dictionaryEntry);
 			templateDictionary.getTemplateEntriesIndex().put(node.getCode(), templateId);
 		}
 		
 		templateDictionaryDAO.save(templateDictionary);
 	}
 
 	/**
 	 * @param patternPath
 	 * @param message
 	 * @return
 	 * @throws ArchiveeException 
 	 */
 	private ArrayList<Template> findTemplates(ContextQueue contextQueue) throws ArchiveeException {
 		ArrayList<Template> templates = new ArrayList<Template>();
 		
 		Template template = new Template();
 		template.getKey().setPatternId(contextQueue.getKey().getPatternId());
 		
 		for(Template t : templateDAO.find(template, ArchiveeConstants.TEMPLATE_KEY_PATTERN_QUERY)) {
 			templates.add(t);
 		}
 		
 		return templates;
 	}
 	
 	/**
 	 * @param patternPath
 	 * @param message
 	 * @return
 	 * @throws ArchiveeException 
 	 */
 	private Template findTemplate(PatternPath patternPath,Pattern pattern) throws ArchiveeException {
 		Template template = new Template();
 		template.getKey().setPatternId(pattern.getId());
 		template.getKey().setPatternPath(patternPath);
 		
 		for(Template t : templateDAO.find(template, ArchiveeConstants.TEMPLATE_KEY_QUERY)) {
 			template = t;
 			break;
 		}
 		
 		return template;
 	}
 
 }
