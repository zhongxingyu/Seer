 package application.filter;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.ws.rs.core.MediaType;
 
 import model.HttpRequestImpl;
 import model.HttpResponseImpl;
 import application.Statistics;
 
 public abstract class Block {
 
 	boolean images;
 	boolean leet;
 	boolean access;
 	Set<InetAddress> ips;
 	Set<String> uris;
 	Set<MediaType> mediaTypes;
 	int maxSize;
 
 	public Block() {
 		this.images = false;
 		this.leet = false;
 		this.access = true;
 		this.ips = new HashSet<InetAddress>();
 		this.uris = new HashSet<String>();
 		this.mediaTypes = new HashSet<MediaType>();
 		this.maxSize = 0;
 	}
 
 	public boolean blockMediaType(final String mediaType) {
 		MediaType m;
 		try {
 			m = MediaType.valueOf(mediaType);
 		} catch (final IllegalArgumentException e) {
 			return false;
 		}
 		if (m == null) {
 			return false;
 		}
 		return this.mediaTypes.add(m);
 	}
 
 	public boolean unlockMediaType(final String mediaType) {
 		final MediaType m = MediaType.valueOf(mediaType);
 		return this.mediaTypes.remove(m);
 	}
 
 	public boolean images() {
 		return this.images;
 	}
 
 	public boolean setMaxSize(final int ms) {
 		this.maxSize = ms;
 		return this.hasMaxSize();
 	}
 
 	public boolean hasMaxSize() {
 		return this.maxSize != 0;
 	}
 
 	public boolean unlockUri(final String uri) {
 		return this.uris.remove(uri);
 	}
 
 	public boolean blockUri(final String regex) {
 		return this.uris.add(regex);
 	}
 
 	public boolean access() {
 		return this.access;
 	}
 
 	public void accessOff() {
 		this.access = false;
 	}
 
 	public void accessOn() {
 		this.access = true;
 	}
 
 	public void leetOn() {
 		this.leet = true;
 	}
 
 	public void leetOff() {
 		this.leet = false;
 	}
 
 	public boolean leet() {
 		return this.leet;
 	}
 
 	public void imagesOff() {
 		this.images = false;
 	}
 
 	public void imagesOn() {
 		this.images = true;
 	}
 
 	public boolean blockIP(final String ip) {
 		try {
			final InetAddress address = InetAddress.getByAddress(ip.getBytes());
 			this.ips.add(address);
 		} catch (final UnknownHostException e) {
 			return false;
 		}
 		return true;
 	}
 
 	public boolean unlockIP(final String ip) {
 
 		try {
			return this.ips.remove(InetAddress.getByAddress(ip.getBytes()));
 		} catch (final UnknownHostException e) {
 			return false;
 		}
 	}
 
 	public abstract HttpResponseImpl doFilter(HttpRequestImpl req,
 			HttpResponseImpl resp, InetAddress ip);
 
 	HttpResponseImpl filter(final HttpRequestImpl req,
 			final HttpResponseImpl resp) {
 		try {
 			if (!this.access) {
 				return ResponseGenerator.generateBlockedResponse(resp);
 			}
 			if (this.destinationIPIsBlocked(req)) {
 				Statistics.getInstance().incrementIpBlocks();
 				return ResponseGenerator.generateBlockedResponseByIp(
 						InetAddress.getByName(req.getHost()).getHostAddress(),
 						resp);
 			}
 			if (this.urisBlocked(req)) {
 				Statistics.getInstance().incrementSiteBlocks();
 				return ResponseGenerator.generateBlockedResponseByUri(req
 						.getRequestURI().toString(), resp);
 			}
 			if (resp.getContentLength() != null) {
 				if (this.maxSize != 0
 						&& Integer.valueOf(resp.getContentLength()) > this.maxSize) {
 					Statistics.getInstance().incrementSizeBlocks();
 					return ResponseGenerator.generateBlockedResponse(
 							this.maxSize, resp);
 				}
 			}
 
 			if (this.isMediaTypeBlockable(resp)) {
 				Statistics.getInstance().incrementContentBlocks();
 				return ResponseGenerator
 						.generateBlockedResponseByMediaType(resp);
 			}
 		} catch (final UnknownHostException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private boolean isMediaTypeBlockable(final HttpResponseImpl response) {
 		for (final MediaType m : this.mediaTypes) {
 			if (response.getHeader("Content-Type") != null) {
 				if (response.getHeader("Content-Type").matches(m.toString())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean urisBlocked(final HttpRequestImpl request) {
 		String requestUri;
 		for (final String regex : this.uris) {
 			requestUri = "http://" + request.getHost()
 					+ request.getRequestURI().toString();
 			final String newString = regex.replace("*", "");
 			if (requestUri.startsWith(newString)) {
 				return true;
 			}
 
 			if (requestUri.matches(regex)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean destinationIPIsBlocked(final HttpRequestImpl request) {
 
 		InetAddress requestIP;
 		try {
 			requestIP = InetAddress.getByName(request.getHost());
 
 			for (final InetAddress ip : this.ips) {
 
 				if ((ip.getHostAddress()).equals(requestIP.getHostAddress())) {
 					return true;
 				}
 			}
 		} catch (final UnknownHostException e) {
 			return false;
 		}
 
 		return false;
 	}
 
 	@Override
 	public abstract boolean equals(Object b);
 }
