 package com.hts.service;
 
 import java.util.List;
 
 import com.hts.entity.BroadcastStream;
 import com.hts.exceptions.AppException;
 
 public interface IBroadcastStreamService {
 
 	void unregisterBroadcastStream(String name) throws AppException;
 	List<BroadcastStream> getAllBroadcastStreams() throws AppException;
 	BroadcastStream getById(Integer streamId) throws AppException;
 	void delete(BroadcastStream broadcastStream) throws AppException;
 
 	List<BroadcastStream> getByName(String name) throws AppException;
 	List<BroadcastStream> getActiveByName(String name) throws AppException;
 	void unregisterAllActiveBroadcastStreams() throws AppException;
 	void setActive(BroadcastStream str) throws AppException;
<<<<<<< HEAD
 	String getJson(List<BroadcastStream> list, String currentPage) throws AppException;
 	List<BroadcastStream> getAllActiveBroadcastStreams() throws AppException;
 	List<BroadcastStream> getAllInactiveBroadcastStreams() throws AppException;
 	List<BroadcastStream> getAllNoChannelBroadcastStreams() throws AppException;
=======
 	BroadcastStream create(String name, String type) throws AppException;
>>>>>>> 2506bdfcf656dc96bdec2580ded1a9fbf98739d2
 }
