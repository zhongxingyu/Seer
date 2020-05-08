 /**
   * Copyright (c) <2011>, <NetEase Corporation>
   * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *    3. Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.netease.webbench.blogbench.misc;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicLong;
 
 import com.netease.webbench.blogbench.dao.BlogDAO;
 import com.netease.webbench.blogbench.dao.BlogDAOFactory;
 import com.netease.webbench.blogbench.misc.ntse.NTSEInitialiser;
 import com.netease.webbench.blogbench.model.Blog;
 import com.netease.webbench.blogbench.model.BlogInfoWithPub;
 import com.netease.webbench.blogbench.statis.ParaDistribution;
 import com.netease.webbench.common.DbOptions;
 import com.netease.webbench.common.DynamicArray;
 import com.netease.webbench.common.Util;
 import com.netease.webbench.random.GammaGenerator;
 import com.netease.webbench.random.ZipfGenerator;
 import com.netease.webbench.resourceReader.BlogResourceReader;
 import com.netease.webbench.resourceReader.ResourceReader;
 
 /**
  * query parameter generator
  * @author LI WEIZHAO
  */
 public class ParameterGenerator {
 	public static final int DEFAULT_MIN_RECORDS_LIMIT = 1000;
 	
 	/* array of character for generate blog title */
 	private final static char[] TITLE = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 
 		'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 
 		'w', 'x', 'y', 'z'};
 	
 	/* random Zipf blog index generator */
 	private ZipfGenerator blgIndexGenerator;
 	
 	/* random Zipf user ID generator */
 	private ZipfGenerator userIdGenerator;
 	
 	/* random blog content length generator */
 	private GammaGenerator contentLenGenerator;
 	
 	/* random number generator */
 	private Random randomGenerator;
 	
 	/* <BlogId,UserId,CurrentTime> pair array */
 	private DynamicArray<BlogInfoWithPub> blgArr;
 	
 	/* blogbench test options */
 	private BbTestOptions bbTestOpt;
 	private DbOptions dbOpt;
 	
 	/* range of blog title length  */
 	private int titleLenRange;	
 	
 	/* range of blog abstract length */
 	private int absRange;	
 	
 	/* max blog id in test table */
 	private AtomicLong maxBlogId = new AtomicLong(0);
 	
 	/* buffer array of blog content */
 	private ArrayList<byte []> cntInitArr;
 	
 	/* blog table run time size */
 	private long tableRunTimeSize;
 	
 	private ParaDistribution paraDist;
 	
 	private ParaInitialiseHandler initialiseHandler;
 
 	/**
 	 * constructor
 	 */
 	public ParameterGenerator() {
 		titleLenRange = 0;
 		absRange = 0;
 		tableRunTimeSize = 0;
 		cntInitArr = null;
 		paraDist = new ParaDistribution();
 	}
 	
 	/**
 	 * create initialise helper
 	 * @param dbSession
 	 * @param dbOpt
 	 * @throws Exception
 	 */
 	private void initParaInitHandler(DbOptions dbOpt) throws Exception {
 		if (0 == "run".compareToIgnoreCase(bbTestOpt.getOperType()) && 
 				dbOpt.getDbType().equalsIgnoreCase("mysql")
 				&& bbTestOpt.getTbEngine().equalsIgnoreCase("ntse")) {
 			this.dbOpt = dbOpt;
 			this.initialiseHandler = new NTSEInitialiser(dbOpt, bbTestOpt);
 		}
 	}
 	
 	/**
 	 * initialise parameter generator
 	 * @param opt
 	 * @param dbOpt
 	 * @throws Exception
 	 */
 	public void init(BbTestOptions opt, DbOptions dbOpt) throws Exception {
 		System.out.println("Is initializing parameter generator...");		
 		this.bbTestOpt = opt;
 		this.dbOpt = dbOpt;
 		
 		initParaInitHandler(dbOpt);
 		if (null != initialiseHandler) {
 			initialiseHandler.doBeforeInit();
 		}
 		
 		//do real initialise work
 		doInit(opt, this.dbOpt);
 		
 		if (null != initialiseHandler) {
 			initialiseHandler.doAfterInit();
 		}
 		System.out.println("Initializing parameter generator done!");
 	}
 
 	/**
 	 * do real initialise work
 	 * @param opt
 	 * @param dbOpt
 	 * @throws Exception
 	 */
 	private void doInit(BbTestOptions opt, DbOptions dbOpt) throws Exception {
 		this.titleLenRange = bbTestOpt.getMaxTtlSize() - bbTestOpt.getMinTtlSize() + 1;		
 		this.absRange = bbTestOpt.getMaxAbsSize() - bbTestOpt.getMinAbsSize() + 1;
 		this.randomGenerator = new Random();
 
 		/* read all blog content files */
 		readBlogResouceFile();
 		
 		int pst = bbTestOpt.getAvgCntSize() - bbTestOpt.getMinCntSize();
 		double beta = 0.5 * pst ;
 		
 		BlogDAO blogDao = BlogDAOFactory.getBlogDAO(dbOpt, bbTestOpt.getUseTwoTable());
		if (0 == "run".compareToIgnoreCase(opt.getOperType())) {				
 			maxBlogId.set(blogDao.selBlogNums() + 1);				
 			blgArr = blogDao.selAllBlogIds();
 			tableRunTimeSize = blgArr.size();
 						
 			if (tableRunTimeSize < DEFAULT_MIN_RECORDS_LIMIT) {
 				throw new Exception("The test table contains too less records: " 
 						+ tableRunTimeSize);
 			}
 			
 			System.out.println("Fetch " + tableRunTimeSize + " records from database.");
 		} else {
 			long tableOldSize = 0;
 			if (!bbTestOpt.isCreateTable()) {
 				tableOldSize = blogDao.selBlogNums();
 				maxBlogId.set(tableOldSize + 1);
 			}
 			
 			tableRunTimeSize = tableOldSize + opt.getTbSize();
 			if (tableRunTimeSize < DEFAULT_MIN_RECORDS_LIMIT) {
 				throw new Exception("The table size specified is too less: " 
 						+ tableRunTimeSize + ", it should larger than " 
 						+ DEFAULT_MIN_RECORDS_LIMIT);
 			}
 		}
 		
 		blgIndexGenerator = new ZipfGenerator(opt.getBlgZipfPct(), opt
 				.getBlgZipfRes(), opt.getBlgZipfPart(), tableRunTimeSize, true, true);
 		/* range of user ID is 1~tableSize/5 */
 		userIdGenerator = new ZipfGenerator(opt.getUserZipfPct(), opt
 				.getUserZipfRes(), opt.getUserZipfPart(),
 				tableRunTimeSize / 5, false, false);
 		contentLenGenerator = new GammaGenerator(beta, pst, 
 				bbTestOpt.getMaxCntSize() - bbTestOpt.getMinCntSize(), true);
 }
 	
 	/**
 	 * read all blog content text files and cache them
 	 * @throws IOException
 	 */	
 	private void readBlogResouceFile() throws IOException {
 		ResourceReader blogResourceFileReader = new BlogResourceReader();
 		cntInitArr = new ArrayList<byte []>(blogResourceFileReader.getResourceFileNum());
 		for (int i = 0; i < blogResourceFileReader.getResourceFileNum(); i++) {
 			InputStream is = blogResourceFileReader.getNextFileAsIntputStream();
 			if (null == is)
 				break;
 			int streamLen = is.available();
 			byte[] buf = new byte[streamLen];
 			is.read(buf, 0, streamLen);
 			
 			byte[] encodeBytes = new String(buf, "GBK").getBytes(
 					Portable.getCharacterSet());			
 			cntInitArr.add(encodeBytes);
 			
 			is.close();
 		}
 	}
 
 	/**
 	 * get blog user ID that follows Zipf distribution
 	 * @return blog user ID
 	 */
 	public long getZipfUserId() {
 		long id = userIdGenerator.getZipfRandomNum();
 		paraDist.getUserIDDis().addResult(id);
 		return id;
 	}
 	
 	/**
 	 * get blog ID that follows Zipf distribution
 	 * @return blog ID
 	 */
 	public long getZipfBlogId() {
 		long blogIndex = blgIndexGenerator.getZipfRandomNum();
 		long blogId = blgArr.get(blogIndex).getBlogId();
 		paraDist.getBlogIDDis().addResult(blogId);
 		return blogId;
 	}
 
 	/**
 	 *  get blog of which ID follows Zipf distribution
 	 * 
 	 * @return
 	 */
 	public BlogInfoWithPub getZipfRandomBlog() {
 		long blogIndex = blgIndexGenerator.getZipfRandomNum();
 		BlogInfoWithPub bi = blgArr.get(blogIndex);
 		paraDist.getBlogIDDis().addResult(bi.getBlogId());		
 		return bi;
 	}
 
 	/**
 	 *  get current time
 	 * @return current time
 	 */
 	public long getCurrentTime() {
 		return Util.currentTimeMillis();
 	}
 
 	/**
 	 * generate new blog title
 	 * @return blog title
 	 */
 	public String getTitle() {	
 		char[] titleBuffer;
 		titleBuffer = new char[bbTestOpt.getMaxTtlSize()];
 		int titleLen = bbTestOpt.getMinTtlSize() + randomGenerator.nextInt(titleLenRange);
 		int i = 0;
 		for (; i < titleLen; i++) {
 			int index = randomGenerator.nextInt(26);
 			titleBuffer[i] = TITLE[index];
 		}
 		String title = new String(titleBuffer, 0, i);
 		return title;
 		
 	}
 	
 	/**
 	 * get random blog content text file index
 	 * @return 
 	 */
 	private int getRandomCntFileIndex() {
 		return randomGenerator.nextInt(cntInitArr.size());
 	}
 
 	/**
 	 * generate blog abstract according blog content
 	 * @param buf  blog content buffer
 	 * @return blog abstract
 	 */
 	public String getAbs(byte[] buf) throws UnsupportedEncodingException {
 		int absLen = bbTestOpt.getMinAbsSize() + randomGenerator.nextInt(absRange);
 		int readLen = absLen > buf.length ? buf.length : absLen;
 
 		byte[] newArr = new byte[readLen];
 		for (int i = 0; i < readLen; i++) {
 			newArr[i] = buf[i];
 		}
 		/* avoid UTF8 illegal character exception */
 		if (newArr[newArr.length - 1] == 0) {
 			newArr[newArr.length - 1] = (byte)32;
 		}
 		
 		return new String(newArr, Portable.getCharacterSet());		
 	}
 
 	/**
 	 * generate new blog content
 	 * @return new blog content
 	 * @throws UnsupportedEncodingException
 	 */
 	public byte[] getContent()  throws Exception {		
 		int cntLen = 0;		
 
 		if (bbTestOpt.isExtraLargeBlog()) {
 			double rdn = randomGenerator.nextDouble();
 
 			/* generate 1% large contents, large content length is between max/2 ~ max,  follows binomial distribution */
 			if (rdn <= 0.01) {
 				cntLen = (int)(bbTestOpt.getMaxCntSize() * (1 + randomGenerator.nextDouble()) / 2);
 			} else {
 				cntLen = bbTestOpt.getMinCntSize() + contentLenGenerator.getGammaRandomNum();	
 			}
 		} else {		
 			cntLen = bbTestOpt.getMinCntSize() + contentLenGenerator.getGammaRandomNum();	
 		}
 				
 		paraDist.getContentLengthDis().addResult(cntLen);
 		
 		ByteBuffer buf = ByteBuffer.allocate(cntLen);
 		int lenBuild = 0;
 		while(lenBuild < cntLen) {
 			int fileIndex = getRandomCntFileIndex();				
 			byte[] byteArr = cntInitArr.get(fileIndex);
 			if (byteArr.length >= (cntLen - lenBuild)) {
 				buf.put(byteArr, 0, cntLen - lenBuild);
 				break;
 			} else {				
 				buf.put(byteArr);
 				lenBuild += byteArr.length;
 			}
 		}		
 		/* avoid UTF8 illegal character exception */
 		if (!buf.hasArray())
 			throw new Exception("Failed to generate blog content test data! ");
 		byte[] checkCnt = buf.array();
 		if (checkCnt[checkCnt.length - 1] == 0) {
 			checkCnt[checkCnt.length - 1] = (byte)32;
 		}
 		return checkCnt;
 	}
 	
 	/**
 	 * generate random allow view
 	 * @return allow view
 	 */
 	public int getAllowView() {
 		double randomPro = randomGenerator.nextDouble();
 		int allowView = -100;
 		if (randomPro < 0.91) {
 			allowView = -100;
 		} else if (randomPro >= 0.92) {
 			allowView = 10000;
 		} else {
 			allowView = 100;
 		}
 		return allowView;
 	}
 
 	/**
 	 * generate new access count
 	 * @return access count
 	 */
 	public long getAccessCount() {
 		return 0;
 	}
 
 	/**
 	 * generate new comment count
 	 * @return comment count
 	 */
 	public long getCommentCount() {
 		return 0;
 	}
 
 	/**
 	 * update blogs array, used when publish new blog
 	 * @param blogId  new blog ID
 	 * @param userId   new blog user ID
 	 * @param publishTime new blog publish time
 	 */
 	public synchronized void updateBlgMapArr(long blogId, long userId,
 			long publishTime) {
 		BlogInfoWithPub newBlog = new BlogInfoWithPub(blogId, userId, publishTime);		
 		blgArr.append(newBlog);
 		blgIndexGenerator.updateProb();
 		tableRunTimeSize++;
 	}
 	
 	/**
 	 * increase current max  blog ID and return it, multi-thread safe
 	 * @return new max blog ID
 	 */
 	public long increaseAndGetMaxBlogId() {
 		return maxBlogId.incrementAndGet();
 	}
 	
 	/**
 	 * get frequency of hottest partition of blogs
 	 * @return 
 	 */
 	public double getBlogHottestPartionFreq() {
 		return blgIndexGenerator.getHottestPartionFreq();
 	}
 	/**
 	 * get frequency of hottest partition of users
 	 * @return 
 	 */
 	public double getUserHottestPartionFreq() {
 		return userIdGenerator.getHottestPartionFreq();
 	}
 	/**
 	 * get frequency of hottest pct% samples of blogs
 	 * @return
 	 */
 	public double getBlogHottestPctFreq() {
 		return blgIndexGenerator.getHottestPctFreq();
 	}
 	/**
 	 *  get frequency of hottest pct% samples of users
 	 * @return
 	 */
 	public double getUserHottestPctFreq() {
 		return userIdGenerator.getHottestPctFreq();
 	}
 	
 	/**
 	 * get a blog from current blogs according Zipf distribution and update it's properties
 	 * Blog ID is selected according Zipf distribution, and publish time, title, content, abstract, allow view are updated.
 	 * This function can be used for update-blog transaction.
 	 * @return new blog
 	 * @throws Exception
 	 */
 	public Blog generateZipfDistrBlog() throws Exception {
 		BlogInfoWithPub blogInfo = getZipfRandomBlog();	
 		long id= blogInfo.getBlogId();
 		long uId = blogInfo.getUId();
 		long pTime = getCurrentTime();			
 		String title = getTitle();		
 		byte[] contentBuf = getContent();	
 		String abs = getAbs(contentBuf);
 		String cnt = new String(contentBuf, Portable.getCharacterSet());		
 		int allowView = getAllowView();
 		
 		return new Blog(id, uId, title, abs, cnt, allowView, pTime, 0, 0);
 	}
 	
 	/**
 	 * generate a new blog
 	 * Blog ID increases according current Blog table, UserID follows Zipf distribution
 	 * @return new Blog
 	 * @throws Exception
 	 */
 	public Blog generateNewBlog() throws Exception {
 		long id= increaseAndGetMaxBlogId();			
 		long uId = getZipfUserId();
 		long pTime = getCurrentTime();			
 		String title = getTitle();		
 		byte[] contentBuf = getContent();
 		String abs = getAbs(contentBuf);		
 		String cnt = new String(contentBuf, Portable.getCharacterSet());	
 		int allowView = getAllowView();
 		
 		return new Blog(id, uId, title, abs, cnt, allowView, pTime, 0, 0);
 	}
 	
 	public ParaDistribution getParaDistribution() {
 		return paraDist;
 	}
 }
