 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2011, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
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
 
 package org.jboss.ejb.client.protocol;
 
 import org.jboss.ejb.client.ModuleID;
 import org.jboss.ejb.client.protocol.marshalling.Marshaller;
 import org.jboss.ejb.client.protocol.marshalling.MarshallerFactory;
 import org.jboss.logging.Logger;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * User: jpai
  */
 public class VersionZeroProtocolHandler implements ClientProtocolHandler {
 
     private static final Logger logger = Logger.getLogger(VersionZeroProtocolHandler.class);
 
    private static final Logger logger = Logger.getLogger(VersionZeroProtocolHandler.class);

     private final byte VERSION = 0x00;
 
     private static final char METHOD_PARAM_TYPE_SEPARATOR = ',';
 
     private final String marshallerType;
 
     private static final byte HEADER_SESSION_OPEN_REQUEST = 0x01;
     private static final byte HEADER_SESSION_OPEN_RESPONSE = 0x02;
     private static final byte HEADER_INVOCATION_REQUEST = 0x03;
     private static final byte HEADER_INVOCATION_CANCEL_REQUEST = 0x04;
     private static final byte HEADER_INVOCATION_RESPONSE = 0x05;
     private static final byte HEADER_INVOCATION_FAILURE = 0x06;
     private static final byte HEADER_MODULE_AVAILABLE = 0x08;
     private static final byte HEADER_MODULE_UNAVAILABLE = 0x09;
 
     private static final Map<Byte, MessageType> knownMessageTypes = new HashMap<Byte, MessageType>();
 
     static {
         knownMessageTypes.put(HEADER_SESSION_OPEN_REQUEST, MessageType.SESSION_OPEN_REQUEST);
         knownMessageTypes.put(HEADER_SESSION_OPEN_RESPONSE, MessageType.SESSION_OPEN_RESPONSE);
         knownMessageTypes.put(HEADER_INVOCATION_REQUEST, MessageType.INVOCATION_REQUEST);
         knownMessageTypes.put(HEADER_INVOCATION_CANCEL_REQUEST, MessageType.INVOCATION_CANCEL_REQUEST);
         knownMessageTypes.put(HEADER_INVOCATION_RESPONSE, MessageType.INVOCATION_RESPONSE);
         knownMessageTypes.put(HEADER_MODULE_AVAILABLE, MessageType.MODULE_AVAILABLE);
         knownMessageTypes.put(HEADER_MODULE_UNAVAILABLE, MessageType.MODULE_UNAVAILABLE);
     }
 
     public VersionZeroProtocolHandler(final String marshallerType) {
         this.marshallerType = marshallerType;
     }
 
     @Override
     public void writeVersionMessage(final DataOutput output, final String marshallerType) throws IOException {
         if (output == null) {
             throw new IllegalArgumentException("Cannot write to null output");
         }
         if (marshallerType == null || marshallerType.isEmpty()) {
             throw new IllegalArgumentException("Marshaller type cannot be null or empty");
         }
         // write the version byte
         output.writeByte(VERSION);
         // write the marshaller type
         output.writeUTF(marshallerType);
     }
 
     @Override
     public void writeSessionOpenRequest(final DataOutput output, final short invocationId, final String appName,
                                         final String moduleName, final String beanName, final String viewClassName,
                                         final Attachment[] attachments) throws IOException {
         if (output == null) {
             throw new IllegalArgumentException("Cannot write to null output");
         }
 
         // write the session open request header
         output.write(HEADER_SESSION_OPEN_REQUEST);
         // write the invocation id
         output.writeShort(invocationId);
         // write the ejb identifier info
         // first write out a flag to indicate whether app name is included
         if (appName != null) {
             output.writeBoolean(true);
             output.writeUTF(appName);
         } else {
             output.writeBoolean(false);
         }
         output.writeUTF(moduleName);
         output.writeUTF(beanName);
         output.writeUTF(viewClassName);
         // write out the attachments
         this.writeAttachments(output, attachments);
 
     }
 
 //    @Override
 //    public void writeSessionOpenResponse(final DataOutput output, final short invocationId, final byte[] sessionId,
 //                                         final Attachment[] attachments) throws IOException {
 //        if (output == null) {
 //            throw new IllegalArgumentException("Cannot write to null output");
 //        }
 //        if (sessionId == null) {
 //            throw new IllegalArgumentException("Session id cannot be null while writing out session open response");
 //        }
 //        // write the session open response header
 //        output.write(MessageType.SESSION_OPEN_RESPONSE.getHeader());
 //        // write the invocation id
 //        output.writeShort(invocationId);
 //        // write session id length
 //        PackedInteger.writePackedInteger(output, sessionId.length);
 //        // write the session id
 //        output.write(sessionId);
 //        // write the attachments
 //        this.writeAttachments(output, attachments);
 //
 //    }
 
     @Override
     public void writeMethodInvocationRequest(final DataOutput output, final short invocationId, final String appName,
                                              final String moduleName, final String beanName, final String viewClassName,
                                              final Method method, final Object[] methodParams,
                                              final Attachment[] attachments) throws IOException {
         if (output == null) {
             throw new IllegalArgumentException("Cannot write to null output");
         }
         if (method == null) {
             throw new IllegalArgumentException("Cannot write out a null method");
         }
         // write the header
         output.writeByte(HEADER_INVOCATION_REQUEST);
         // write the invocation id
         output.writeShort(invocationId);
         // write the ejb identifier info
         // first write out a flag to indicate whether app name is included
         if (appName != null) {
             output.writeBoolean(true);
             output.writeUTF(appName);
         } else {
             output.writeBoolean(false);
         }
         output.writeUTF(moduleName);
         output.writeUTF(beanName);
         output.writeUTF(viewClassName);
         // method name
         output.writeUTF(method.getName());
         // param types
         final Class<?>[] methodParamTypes = method.getParameterTypes();
         final StringBuffer methodSignature = new StringBuffer();
         for (int i = 0; i < methodParamTypes.length; i++) {
             methodSignature.append(methodParamTypes[i].getName());
             if (i != methodParamTypes.length - 1) {
                 methodSignature.append(METHOD_PARAM_TYPE_SEPARATOR);
             }
         }
         // write the method signature
         output.writeUTF(methodSignature.toString());
         // write out the attachments
         this.writeAttachments(output, attachments);
 
         // marshall the method params
         final Marshaller marshaller = MarshallerFactory.createMarshaller(this.marshallerType);
         marshaller.start(output);
         for (int i = 0; i < methodParams.length; i++) {
             marshaller.writeObject(methodParams[i]);
         }
         marshaller.finish();
 
     }
 
     @Override
     public void writeInvocationCancelRequest(final DataOutput output, final short invocationId) throws IOException {
         if (output == null) {
             throw new IllegalArgumentException("Cannot write to null output");
         }
 
         // write invocation cancel request header
         output.write(HEADER_INVOCATION_CANCEL_REQUEST);
         // write the invocation id
         output.writeShort(invocationId);
     }
 
     @Override
     public MessageType getMessageType(DataInput input) throws IOException {
         final byte header = input.readByte();
         final MessageType messageType = knownMessageTypes.get(header);
         if (messageType != null) {
             return messageType;
         }
         throw new IOException("Unidentified message header 0x" + Integer.toHexString(header));
     }
 
 //    @Override
 //    public MethodInvocationRequest readMethodInvocationRequest(final DataInput input, final EJBViewResolver ejbViewResolver) throws IOException {
 //        // read the invocation id
 //        final short invocationId = input.readShort();
 //        // read the ejb identifier info
 //        // first read a flag to check whether app name is included
 //        final boolean appNamePresent = input.readBoolean();
 //        String appName = null;
 //        if (appNamePresent) {
 //            appName = input.readUTF();
 //        }
 //        final String moduleName = input.readUTF();
 //        final String beanName = input.readUTF();
 //        final String viewClassName = input.readUTF();
 //        // resolve the EJB view
 //        final EJBViewResolutionResult viewResolutionResult = ejbViewResolver.resolveEJBView(appName, moduleName, beanName, viewClassName);
 //        final String methodName = input.readUTF();
 //        // method signature
 //        String[] methodParamTypes = null;
 //        final String signature = input.readUTF();
 //        if (signature.isEmpty()) {
 //            methodParamTypes = new String[0];
 //        } else {
 //            methodParamTypes = signature.split(String.valueOf(METHOD_PARAM_TYPE_SEPARATOR));
 //        }
 //        // read the attachments
 //        final Attachment[] attachments = this.readAttachments(input);
 //
 //        // un-marshall the method params
 //        final Object[] methodParams = new Object[methodParamTypes.length];
 //        final UnMarshaller unMarshaller = MarshallerFactory.createUnMarshaller(this.marshallerType);
 //        unMarshaller.start(input, viewResolutionResult.getEJBClassLoader());
 //        for (int i = 0; i < methodParamTypes.length; i++) {
 //            try {
 //                methodParams[i] = unMarshaller.readObject();
 //            } catch (ClassNotFoundException cnfe) {
 //                throw new RuntimeException(cnfe);
 //            }
 //        }
 //        unMarshaller.finish();
 //
 //        return new MethodInvocationRequest(invocationId, appName, moduleName, beanName,
 //                viewClassName, methodName, methodParamTypes, methodParams, attachments);
 //    }
 
 //    @Override
 //    public void writeMethodInvocationResponse(final DataOutput output, final short invocationId, final Object result,
 //                                              final Throwable error, final Attachment[] attachments) throws IOException {
 //        if (output == null) {
 //            throw new IllegalArgumentException("Cannot write to null output");
 //        }
 //
 //        // write invocation response header
 //        output.write(MessageType.INVOCATION_RESPONSE.getHeader());
 //        // write the invocation id
 //        output.writeShort(invocationId);
 //        // write the attachments
 //        this.writeAttachments(output, attachments);
 //        // write a flag to indicate whether the invocation response is a exception
 //        if (error != null) {
 //            output.writeBoolean(true);
 //        } else {
 //            output.writeBoolean(false);
 //        }
 //
 //        // write out the result/exception
 //        final Marshaller marshaller = MarshallerFactory.createMarshaller(this.marshallerType);
 //        marshaller.start(output);
 //        if (error != null) {
 //            // write out the excption
 //            marshaller.writeObject(error);
 //        } else {
 //            // write out the result
 //            marshaller.writeObject(result);
 //        }
 //        marshaller.finish();
 //    }
 
 //    @Override
 //    public void writeModuleAvailability(final DataOutput output, final EJBModuleIdentifier[] ejbModuleIdentifiers) throws IOException {
 //        if (output == null) {
 //            throw new IllegalArgumentException("Cannot write to null output");
 //        }
 //        if (ejbModuleIdentifiers == null) {
 //            throw new IllegalArgumentException("EJB module identifiers cannot be null");
 //        }
 //        // write the header
 //        output.write(MessageType.MODULE_AVAILABLE.getHeader());
 //        // write the count
 //        PackedInteger.writePackedInteger(output, ejbModuleIdentifiers.length);
 //        // write the app/module names
 //        for (int i = 0; i < ejbModuleIdentifiers.length; i++) {
 //            // write the app name
 //            final String appName = ejbModuleIdentifiers[i].getAppName();
 //            if (appName == null) {
 //                // write out a empty string
 //                output.writeUTF("");
 //            } else {
 //                output.writeUTF(appName);
 //            }
 //            // write the module name
 //            output.writeUTF(ejbModuleIdentifiers[i].getModuleName());
 //            // write the distinct name
 //            final String distinctName = ejbModuleIdentifiers[i].getDistinctName();
 //            if (distinctName == null) {
 //                // write out an empty string
 //                output.writeUTF("");
 //            } else {
 //                output.writeUTF(distinctName);
 //            }
 //        }
 //    }
 
 //    @Override
 //    public MethodInvocationResponse readMethodInvocationResponse(final DataInput input, final InvocationClassLoaderResolver classLoaderResolver) throws IOException {
 //        if (input == null) {
 //            throw new IllegalArgumentException("Cannot read from null input");
 //        }
 //
 //        // read the invocation id
 //        final short invocationId = input.readShort();
 //        final UnMarshaller unMarshaller = MarshallerFactory.createUnMarshaller(this.marshallerType);
 //        // read the attachments
 //        this.readAttachments(input);
 //        // check exception flag
 //        boolean exception = input.readBoolean();
 //        final ClassLoader classLoader = classLoaderResolver.resolveClassLoader(invocationId);
 //        unMarshaller.start(input, classLoader);
 //
 //        Throwable t = null;
 //        Object result = null;
 //        try {
 //            if (exception) {
 //                // read the excption
 //                t = (Throwable) unMarshaller.readObject();
 //            } else {
 //                // read the result
 //                result = unMarshaller.readObject();
 //            }
 //        } catch (ClassNotFoundException e) {
 //            throw new RuntimeException(e);
 //        }
 //        unMarshaller.finish();
 //
 //        return new MethodInvocationResponse(invocationId, result, t);
 //    }
 
 //    @Override
 //    public Message readSessionOpenRequest(DataInput input) throws IOException {
 //        if (input == null) {
 //            throw new IllegalArgumentException("Cannot read from null input");
 //        }
 //
 //        // read the invocation id
 //        final short invocationId = input.readShort();
 //        // read the ejb identifier info
 //        // first read a flag to check whether app name is included
 //        final boolean appNamePresent = input.readBoolean();
 //        String appName = null;
 //        if (appNamePresent) {
 //            appName = input.readUTF();
 //        }
 //        final String moduleName = input.readUTF();
 //        final String beanName = input.readUTF();
 //        final String viewClassName = input.readUTF();
 //        // read the attachments
 //        final Attachment[] attachments = this.readAttachments(input);
 //
 //        return new Message(invocationId, appName, moduleName, beanName, viewClassName, attachments);
 //    }
 
     @Override
     public ModuleID[] readModuleAvailability(DataInput input) throws IOException {
         if (input == null) {
             throw new IllegalArgumentException("Cannot read from null input");
         }
         // read the count
         final int count = PackedInteger.readPackedInteger(input);
         final ModuleID[] ejbModules = new ModuleID[count];
         // read the app/module names
         for (int i = 0; i < ejbModules.length; i++) {
             // read the app name
             String appName = input.readUTF();
             if (appName.isEmpty()) {
                 appName = null;
             }
             // read the module name
             final String moduleName = input.readUTF();
             String distinctName = input.readUTF();
             if (distinctName.isEmpty()) {
                 distinctName = null;
             }
             ejbModules[i] = new ModuleID(appName, moduleName, distinctName);
         }
         return ejbModules;
     }
 
     private void writeAttachments(final DataOutput output, final Attachment[] attachments) throws IOException {
         if (attachments == null) {
             output.writeByte(0);
             return;
         }
         // write attachment count
         output.writeByte(attachments.length);
         for (int i = 0; i < attachments.length; i++) {
             // write attachment id
             output.writeShort(attachments[i].getId());
             final byte[] data = attachments[i].getData();
             // write data length
             PackedInteger.writePackedInteger(output, data.length);
             // write the data
             output.write(data);
         }
     }
 
     private Attachment[] readAttachments(final DataInput input) throws IOException {
         int numAttachments = input.readByte();
         if (numAttachments == 0) {
             return new Attachment[0];
         }
         final Attachment[] attachments = new Attachment[numAttachments];
         for (int i = 0; i < numAttachments; i++) {
             // read attachment id
             final short attachmentId = input.readShort();
             // read attachment data length
             final int dataLength = PackedInteger.readPackedInteger(input);
             // read the data
             final byte[] data = new byte[dataLength];
             input.readFully(data);
 
             final Attachment attachment = new Attachment(attachmentId, data);
             attachments[i] = attachment;
         }
         return attachments;
     }
 }
