 /*******************************************************************************
<<<<<<< HEAD:vorpex-server/src/main/java/vorpex/net/PipelineFactory.java
 * <copyright file="PipelineFactory.java" company="VorpeX">
=======
  * <copyright file="Vorpex.java" company="VorpeX">
>>>>>>> 6dd91ff878f418e92e0ab0d473cf6d92434f4983:vorpex-server/src/main/java/vorpex/net/PipelineFactory.java
  *  Copyright (c) 2011-2012 All Right Reserved, http://vorpex.biz/
  *
  *  This source is subject to the "Don't Be A Dick" License.
  *  Please see the License.txt file for more information.
  *  You may not use this file except in compliance with the License.
  *
  *  THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
  *  KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  *  IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
  *  PARTICULAR PURPOSE.
  *
  *  @author Dominic Gunn
  *  @email d.gunn@vorpex.biz
  *  @date 19-12-2012
  *  @summary
  ******************************************************************************/
 package vorpex.net;
 
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 
 /**
  * Class used for the initialisation of ChannelPipeLines.
<<<<<<< HEAD:vorpex-server/src/main/java/vorpex/net/PipelineFactory.java
  * @author Dominic Gunn (d.gunn@vorpex.biz)
=======
 * @author Dominic (d.gunn@vorpex.biz)
>>>>>>> 6dd91ff878f418e92e0ab0d473cf6d92434f4983:vorpex-server/src/main/java/vorpex/net/PipelineFactory.java
  */
 public class PipelineFactory implements ChannelPipelineFactory {
 
 	/**
 	 * Function used to get the various pipes for
 	 * Netty (handler, encoder, decoder, etc.).
 	 */
 	public final ChannelPipeline getPipeline() throws Exception {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
