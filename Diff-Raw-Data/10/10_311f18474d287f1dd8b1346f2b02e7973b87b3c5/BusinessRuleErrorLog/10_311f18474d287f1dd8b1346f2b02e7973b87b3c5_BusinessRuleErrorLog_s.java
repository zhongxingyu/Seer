 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark
  * (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.haiba.lprimporter.log;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 
 import dk.nsi.haiba.lprimporter.dao.HAIBADAO;
 import dk.nsi.haiba.lprimporter.message.MessageResolver;
 import dk.nsi.haiba.lprimporter.rules.BusinessRuleError;
 
 public class BusinessRuleErrorLog {
 	
 	private static Logger businessRuleErrorLog = Logger.getLogger("BusinessRulesErrors");
 
 	@Value("${disable.database.errorlog}")
 	boolean disableDatabaseErrorLog;
 
 	@Autowired
 	MessageResolver resolver;
 
 	@Autowired
 	HAIBADAO haibaDao;
 
 	public void log(BusinessRuleError be) {
 		if(!disableDatabaseErrorLog) {
 			// Save Businessrule error in database
 			haibaDao.saveBusinessRuleError(be);
 		}
 		
		businessRuleErrorLog.info(resolver.getMessage("errorlog.rule.message", new Object[] {""+be.getLprReference(), be.getAbortedRuleName(), be.getDescription()}));
 	}
 
 }
