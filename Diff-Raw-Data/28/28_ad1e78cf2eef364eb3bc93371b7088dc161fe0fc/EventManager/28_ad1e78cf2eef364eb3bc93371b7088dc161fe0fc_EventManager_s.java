 /*
  * Copyright (c) 2010 Eugene Prokopiev <enp@itx.ru>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ru.itx.ccm.beans;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ru.itx.ccm.model.Call;
 import ru.itx.ccm.model.Counter;
 import ru.itx.ccm.model.Session;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.List;
 
 public class EventManager {
 
 	private Logger logger = LoggerFactory.getLogger(getClass());
 
 	@PersistenceContext
 	private EntityManager em;
 
 	public void connectSession(String systemId, String userName, String userAgent, String host) {
 		Session session = new Session(systemId, userName, userAgent, host);
 		em.persist(session);
 		logger.debug("session connect: {}, {}, {}", new Object[] {userName, userAgent, host});
 	}
 
 	public void disconnectSession(String systemId) {
 		List<Session> sessions = (List<Session>)
 			em.createQuery("select s from Session s where systemId = :systemId")
 			.setParameter("systemId", systemId)
 			.getResultList();
 		for (Session session : sessions) {
 			session.disconnect();
 			em.persist(session);
 			logger.debug("session disconnect: {}", session.getUserName());
 		}
 	}
 
 	public void connectCall(String systemId, String source, String destination, String fifoName) {
 		Call call = new Call(systemId, source, destination, fifoName);
 		em.persist(call);
 		logger.debug("call connect: {}->{}", new Object[] {source, destination});
 	}
 
 	private List<Call> getCalls(String systemId) {
 		return (List<Call>)em.createQuery("select c from Call c where systemId = :systemId")
 			.setParameter("systemId", systemId)
 			.getResultList();
 	}
 
 	public void answerCall(String systemId, final String userName) {
 		for (Call call : getCalls(systemId)) {
 			call.answer(userName);
 			em.persist(call);
			logger.debug("call aswer: {}->{}->{}", new Object[] {call.getSource(), call.getDestination(), userName});
 		}
 	}
 
 	public void hangupCall(String systemId) {
 		for (Call call : getCalls(systemId)) {
 			call.hangup();
 			em.persist(call);
 			logger.debug("call hangup: {}->{}", new Object[] {call.getSource(), call.getDestination()});
 		}
 	}
 
 	public void abortCall(String systemId) {
 		for (Call call : getCalls(systemId)) {
 			call.abort();
 			em.persist(call);
 			logger.debug("call abort: {}->{}", new Object[] {call.getSource(), call.getDestination()});
 		}
 	}
 
 	public void failCall(String systemId, String userName, String reason) {
 		for (Call call : getCalls(systemId)) {
 				call.fail(userName, reason);
 				em.persist(call);
 				logger.debug("call fail: {}->{}->{} - {}",
 					new Object[] {call.getSource(), call.getDestination(), userName, reason});
 			}
 		}
 
 	public void count(String fifo, int members, int activeMembers, int callers, int bridges) {
 		Counter counter = new Counter(fifo, members, activeMembers, callers, bridges);
 		em.persist(counter);
 		logger.debug("counter: {} - {},{},{},{}",
 			new Object[] {fifo, members, activeMembers, callers, bridges});
 	}
 }
