 package com.amdalal.data.aerospike.service.impl;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.springframework.stereotype.Service;
 
 import com.aerospike.client.AerospikeClient;
 import com.aerospike.client.AerospikeException;
 import com.aerospike.client.Bin;
 import com.aerospike.client.Key;
 import com.aerospike.client.Record;
 import com.aerospike.client.policy.WritePolicy;
 import com.amdalal.data.aerospike.core.GenericVOWrapper;
 import com.amdalal.data.aerospike.exception.DuplicateBinNameException;
 import com.amdalal.data.aerospike.exception.GetFailedException;
 import com.amdalal.data.aerospike.exception.KeyNotDefinedException;
 import com.amdalal.data.aerospike.exception.LoadFailedException;
 import com.amdalal.data.aerospike.exception.PutFailedException;
 import com.amdalal.data.aerospike.exception.RemoveFailedException;
 import com.amdalal.data.aerospike.helper.ClassReader;
 import com.amdalal.data.aerospike.helper.SetterGetterHelper;
 import com.amdalal.data.aerospike.helper.StringUtils;
 import com.amdalal.data.aerospike.service.IAerospikeService;
 import com.amdalal.data.aerospike.service.VOMarker;
 
 @Service
 public class AerospikeServiceImpl implements IAerospikeService {
 
     private static final WritePolicy                       policy             = new WritePolicy();
 
     private Map<Class<? super VOMarker>, GenericVOWrapper> markerToWrapperMap = new HashMap<Class<? super VOMarker>, GenericVOWrapper>();
 
     private AerospikeClient                                client;
 
     @Override
     public void put(VOMarker vo) throws PutFailedException {
         GenericVOWrapper voWrapper = markerToWrapperMap.get(vo.getClass());
         Bin[] bins = new Bin[voWrapper.getBinNameToGetterMap().size()];
         int index = 0;
         for (Entry<String, Method> e : voWrapper.getBinNameToGetterMap().entrySet()) {
             try {
                 bins[index] = new Bin(e.getKey(), e.getValue().invoke(vo, new Object[] {}));
             } catch (IllegalArgumentException e1) {
                 throw new PutFailedException("Failed to invoke getter method on VO", e1);
             } catch (IllegalAccessException e1) {
                 throw new PutFailedException("Failed to invoke getter method on VO", e1);
             } catch (InvocationTargetException e1) {
                 throw new PutFailedException("Failed to invoke getter method on VO", e1);
             }
             index++;
         }
         Key key = null;
         try {
             key = new Key(voWrapper.getNamespace(), voWrapper.getSet(), voWrapper.getBinNameToGetterMap().get(voWrapper.getKeyBinName()).invoke(vo, new Object[] {}));
         } catch (IllegalArgumentException e1) {
             throw new PutFailedException("Failed to invoke getter method on VO while fetching key", e1);
         } catch (AerospikeException e1) {
             throw new PutFailedException("Failed to invoke getter method on VO while fetching key", e1);
         } catch (IllegalAccessException e1) {
             throw new PutFailedException("Failed to invoke getter method on VO while fetching key", e1);
         } catch (InvocationTargetException e1) {
             throw new PutFailedException("Failed to invoke getter method on VO while fetching key", e1);
         }
         try {
             client.put(policy, key, bins);
         } catch (AerospikeException e1) {
             throw new PutFailedException("Failed while putting VO in Aerospike", e1);
         }
     }
 
     @Override
    public VOMarker get(String key, Class<? extends VOMarker> clazz) throws GetFailedException {
         GenericVOWrapper voWrapper = markerToWrapperMap.get(clazz);
         Record record = null;
         try {
            record = client.get(policy, new Key(voWrapper.getNamespace(), voWrapper.getSet(), key));
         } catch (AerospikeException e2) {
             throw new GetFailedException("AerospikeException", e2);
         }
         VOMarker vo = null;
         if (record != null) {
             try {
                 vo = (VOMarker) clazz.newInstance();
             } catch (InstantiationException e1) {
                 throw new GetFailedException("Failed to instantiate VO", e1);
             } catch (IllegalAccessException e1) {
                 throw new GetFailedException("Failed to instantiate VO", e1);
             }
             for (Entry<String, Method> e : voWrapper.getBinNameToSetterMap().entrySet()) {
                 try {
                     e.getValue().invoke(vo, record.getValue(e.getKey()));
                 } catch (IllegalArgumentException e1) {
                     throw new GetFailedException("Failed to invoke setter on VO", e1);
                 } catch (IllegalAccessException e1) {
                     throw new GetFailedException("Failed to invoke setter on VO", e1);
                 } catch (InvocationTargetException e1) {
                     throw new GetFailedException("Failed to invoke setter on VO", e1);
                 }
             }
         }
         return vo;
     }
 
     @Override
    public boolean remove(String key, Class<? extends VOMarker> clazz) throws RemoveFailedException {
         GenericVOWrapper voWrapper = markerToWrapperMap.get(clazz);
         try {
            return client.delete(policy, new Key(voWrapper.getNamespace(), voWrapper.getSet(), key));
         } catch (AerospikeException aex) {
            throw new RemoveFailedException("Failed to remove key: " + key, aex);
         }
     }
 
     @Override
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void loadAerospikeConfig(String endPoint, String packageToScan) throws LoadFailedException {
         try {
             initializeClient(endPoint);
             for (Class clazz : ClassReader.getClasses(packageToScan)) {
                 com.amdalal.data.aerospike.annotation.Set setAnnotation = (com.amdalal.data.aerospike.annotation.Set) clazz.getAnnotation(com.amdalal.data.aerospike.annotation.Set.class);
                 if (setAnnotation != null) {
                     GenericVOWrapper voWrapper = new GenericVOWrapper();
                     voWrapper.setNamespace(setAnnotation.namespace());
                     voWrapper.setSet(setAnnotation.name());
                     for (Field f : clazz.getDeclaredFields()) {
                         com.amdalal.data.aerospike.annotation.Bin binAnnotation = (com.amdalal.data.aerospike.annotation.Bin) f.getAnnotation(com.amdalal.data.aerospike.annotation.Bin.class);
                         if (binAnnotation != null) {
                             if (voWrapper.getBinNameToGetterMap().put(binAnnotation.name(), clazz.getMethod(SetterGetterHelper.getGetterName(f), new Class[] {})) != null) {
                                 throw new DuplicateBinNameException("Duplicate field Name: " + f.getName());
                             }
                             if (voWrapper.getBinNameToSetterMap().put(binAnnotation.name(), clazz.getMethod(SetterGetterHelper.getSetterName(f), new Class[] { f.getType() })) != null) {
                                 throw new DuplicateBinNameException("Duplicate field Name: " + f.getName());
                             }
                         }
                         com.amdalal.data.aerospike.annotation.Key keyAnnotation = (com.amdalal.data.aerospike.annotation.Key) f.getAnnotation(com.amdalal.data.aerospike.annotation.Key.class);
                         if (keyAnnotation != null) {
                             voWrapper.setKeyBinName(binAnnotation.name());
                         }
                     }
                     if (StringUtils.isEmpty(voWrapper.getKeyBinName())) {
                         throw new KeyNotDefinedException("No Key defined for Set: " + setAnnotation.name() + " in namespace: " + setAnnotation.namespace());
                     }
                     markerToWrapperMap.put(clazz, voWrapper);
                 }
             }
         } catch (SecurityException e) {
             throw new LoadFailedException(e);
         } catch (ClassNotFoundException e) {
             throw new LoadFailedException(e);
         } catch (IOException e) {
             throw new LoadFailedException(e);
         } catch (NoSuchMethodException e) {
             throw new LoadFailedException(e);
         } catch (DuplicateBinNameException e) {
             throw new LoadFailedException(e);
         } catch (KeyNotDefinedException e) {
             throw new LoadFailedException(e);
         } catch (AerospikeException e) {
             throw new LoadFailedException(e);
         }
     }
 
     private void initializeClient(String endPoint) throws AerospikeException {
         List<String> endPointSplit = StringUtils.split(endPoint, ":");
         client = new AerospikeClient(endPointSplit.get(0).trim(), Integer.parseInt(endPointSplit.get(1).trim()));
     }
 }
