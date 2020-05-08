 /**
 * Light agent framework.
  * 
  * <h1>What is?</h1>
  * <p>
  * Offers a platform to write/plug-in functionality. There are many such platforms but I could not
 * decide on a particular one to use, although for basic service lifecycle, OSGI seems proper.
  * 
  * <p>
  * What is special about these agents is that, while offering a simple platform to plug-in
  * functionality, they will offer advanced services for distributed communication and cooperation,
  * i.e. "onConnectToOtherAgent()".
  * 
  * <p>
  * Agents form groups which cooperate, best example for that being all the devices in the home (2
  * laptops, 1 desktop, 1 multimedia desktop, 2 tablets, 1 PSP, 2 game consoles). That's 9 computing
  * devices, without counting ipods, cell phones and such.
  * 
  * <p>
  * Given that, there's obviously a need to manage certain services on all those, such as "favorites"
  * etc.
  * 
  * STATE: post-concept, changes but not often
  * 
  * <h1>Developer's Guide</h1>
  * 
  * Write your functionality as an AgentService. Then, in the main(), setup an Agent and add your service.
  * 
  * See SampleJavaService.java and associated unit tests.
  * 
  * @version $Id: package-info.java,v 1.1 2007-10-02 11:54:36 razvanc Exp $
  */
 package com.razie.pub.agent;
 
