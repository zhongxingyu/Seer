 /*
  * The PID webservice offers SOAP methods to manage the Handle System(r) resolution technology.
  *
  * Copyright (C) 2010-2011, International Institute of Social History
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.socialhistoryservices.pid.service;
 
 import net.handle.hdllib.HandleException;
 import net.handle.hdllib.Util;
 import org.apache.taglibs.standard.lang.jstl.NullLiteral;
 import org.socialhistoryservices.pid.database.dao.HandleDao;
 import org.socialhistoryservices.pid.database.dao.HandleDaoImpl;
 import org.socialhistoryservices.pid.database.domain.Handle;
 import org.socialhistoryservices.pid.exceptions.PidException;
 import org.socialhistoryservices.pid.schema.LocAttType;
 import org.socialhistoryservices.pid.schema.PidType;
 import org.socialhistoryservices.pid.security.NAAuthentication;
 import org.socialhistoryservices.pid.util.PidGenerator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.oxm.jaxb.Jaxb2Marshaller;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.transform.stream.StreamSource;
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Lucien van Wouw <lwo@iisg.nl>
  * @since 2011-01-01
  */
 public class StubPidResourceService implements PidResourceService {
 
     @Value("#{pidProperties['handle.baseUrl']}")
     private String handleBaseUrl = "";
 
     @Autowired
     private HandleDao handleDao;
 
     private NAAuthentication NAAuthentication;
 
     @Autowired
     Jaxb2Marshaller marshaller;
 
     final private static byte[] bURL = HandleDaoImpl.URL.getBytes();
     final private static String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><locations xmlns=\"http://pid.socialhistoryservices.org/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"locAttType\">";
 
     @Override
     public PidType upsertPid(String na, PidType pidType) throws HandleException {
 
         NAAuthentication.authorize(na);
         String lidCandidate = pidType.getLocalIdentifier();
         if (lidCandidate != null) {
             final List<Handle> h = handleDao.fetchHandleByAttribute(na, lidCandidate, "LID");
            if (h.size() != 0) {
                 final String pid = h.get(0).getHandle();
                 if (pidType.getPid() == null) {
                     pidType.setPid(pid);
                 } else if (!pid.equalsIgnoreCase(pidType.getPid())) {
                    throw new PidException("Cannot create local identifier " + lidCandidate + " because it already is bound to PID " + pid);
                 }
             }
         }
         setDefault(na, pidType);
         return convertHandleToPidType(handleDao.upsertHandle(na, pidType));
     }
 
     @Override
     public PidType createPid(String na, PidType pidType) throws HandleException {
 
         NAAuthentication.authorize(na);
         setDefault(na, pidType);
         String lidCandidate = pidType.getLocalIdentifier();
         if (lidCandidate != null) {
             final List<Handle> handles = handleDao.fetchHandleByAttribute(na, lidCandidate, "LID");
             if (handles.size() != 0)
                 throw new PidException("Cannot create local identifier " + lidCandidate + " because it already is bound to PID " + handles.get(0).getHandle());
         }
         final List<Handle> handles = handleDao.createNewHandle(na, pidType);
         return convertHandleToPidType(handles);
     }
 
     private void setDefault(String na, PidType pidType) {
         if (pidType.getResolveUrl() == null)
             pidType.setResolveUrl(handleBaseUrl);
 
         if (pidType.getPid() == null) {
             pidType.setPid(PidGenerator.getPid(na)); // No need to check for existing PIDs. Too small a change.
         } else {
             String pidCandidate = PidGenerator.validatePid(na, pidType.getPid());
             pidType.setPid(pidCandidate);
         }
     }
 
     @Override
     public PidType updatePid(PidType handle) throws HandleException {
 
         String na = NAAuthentication.authorize(handle.getPid());
         String lidCandidate = handle.getLocalIdentifier();
         checkAvailability(handle.getPid(), na, lidCandidate);
        return convertHandleToPidType( handleDao.updateHandle(na, handle));
     }
 
     private void checkAvailability(String pid, String na, String lidCandidate) {
         if (lidCandidate != null) { // Check if we are not used elsewhere...
             final List<Handle> handles = handleDao.fetchHandleByAttribute(na, lidCandidate, "LID");
             for (Handle h : handles) {
                 if (h.getHandle().equals(pid)) {
                     return;
                 }
                 throw new PidException("Cannot create local identifier "
                         + lidCandidate
                         + " because it already is bound to PID " + handles.get(0).getHandle());
             }
         }
     }
 
     @Override
     public PidType getPid(String pidId) throws HandleException {
         String normalisedPidId = pidId;
         if (pidId.startsWith(handleBaseUrl)) {
             normalisedPidId = pidId.replaceFirst(handleBaseUrl, "");
         }
         String na = NAAuthentication.authorize(normalisedPidId);
         return convertHandleToPidType(handleDao.fetchHandleByPID(na, normalisedPidId));
     }
 
     @Override
     public void getPidByAttribute(List<PidType> ht, String na, String attribute) {
 
         final java.util.List fetchHandles = handleDao.fetchHandleByAttribute(NAAuthentication.authorize(na), attribute, null);
         List<PidType> handles = convertHandlesToPidType(fetchHandles);
         for (PidType handle : handles) {
             ht.add(handle);
         }
     }
 
     @Override
     public PidType createHopePid(String na, String lid, String resolveUrl) throws HandleException {
 
         if (lid == null)
             throw new PidException("The local identifier is not in the request.");
         if (resolveUrl == null)
             resolveUrl = "";
         List<Handle> handles = handleDao.fetchHandleByAttribute(NAAuthentication.authorize(na), lid, "LID");
         if (handles.isEmpty()) {
             PidType pidType = new PidType();
             pidType.setPid(PidGenerator.getPid(na));
             pidType.setResolveUrl(resolveUrl);
             pidType.setLocalIdentifier(lid);
             handles = handleDao.createNewHandle(na, pidType);
         } else {
             for (Handle handle : handles) {
                 if (Arrays.equals(handle.getType(), bURL) &&
                         !handle.getDataAsString().equals(resolveUrl)) {
                     PidType pidType = new PidType();
                     pidType.setPid(handle.getHandle());
                     pidType.setResolveUrl(resolveUrl);
                     pidType.setLocalIdentifier(lid);
                     return updatePid(pidType);
                 }
             }
         }
         return convertHandleToPidType(handles);
     }
 
     @Override
     public boolean deletePid(String pid) {
         NAAuthentication.authorize(pid);
         return handleDao.deletePid(pid);
     }
 
     @Override
     public long deletePids(String na) {
 
         if (na == null || !na.contains("."))
             throw new PidException("The naming authority is not a test prefix. Test prefixes contain a dot.");
         return handleDao.deletePids(NAAuthentication.authorize(na));
     }
 
     private PidType convertHandleToPidType(List<Handle> handles) {
 
         return (handles.size() == 0)
                 ? null
                 : convertHandlesToPidType(handles).get(0);
     }
 
     /**
      * A handle in the Handle System logic is a single identifier with a type.
      * As there can be several types ( URL, LID, 10320/loc ) these are added to the
      * PidType as one entity.
      * <p/>
      * In a reverse lookup, there may be several PidTypes also. Here we group a
      * PidType by pid.
      *
      * @param handles The Pid records from the database
      * @return The PID type records for the endpoint
      */
     private List<PidType> convertHandlesToPidType(List<Handle> handles) {
 
         final List<PidType> pids = new ArrayList(handles.size());
         for (Handle handle : handles) {
 
             PidType pid = getHandle(handle.getHandle(), pids);
             String type = handle.getTypeAsString();
             if (type.equals(HandleDaoImpl.URL)) {
                 pid.setResolveUrl(handle.getDataAsString());
             } else if (type.equals(HandleDaoImpl.LOC)) {
                 pid.setLocAtt(getLocations(handle));
             } else if (type.equals(HandleDaoImpl.LID))
                 pid.setLocalIdentifier(handle.getDataAsString());
         }
 
         return pids;
     }
 
     public LocAttType getLocations(Handle handle) {
 
         final String dataAsString = xml + handle.getDataAsString().substring(11);
         ByteArrayInputStream is = new ByteArrayInputStream(Util.encodeString(dataAsString));
         StreamSource source = new StreamSource(is);
         JAXBElement element = (JAXBElement) marshaller.unmarshal(source);
         return (LocAttType) element.getValue();
     }
 
     private PidType getHandle(String pid, List<PidType> handles) {
         for (PidType pidType : handles) {
             if (pidType.getPid().equals(pid))
                 return pidType;
         }
         PidType handle = new PidType();
         handle.setPid(pid);
         handles.add(handle);
         return handle;
     }
 
     public void setNAAuthentication(NAAuthentication NAAuthentication) {
         this.NAAuthentication = NAAuthentication;
     }
 }
